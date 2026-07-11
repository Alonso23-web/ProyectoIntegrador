package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IPasajeService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints REST usados por cliente-buscar.js.
 * El cliente NO elige horario: busca por ruta + fecha, y el sistema
 * le asigna el viaje con cupos disponibles para ese día automáticamente.
 */
@RestController
@RequestMapping("/api/pasajes")
@RequiredArgsConstructor
public class PasajeRestController {

    private final IPasajeService pasajeService;
    private final ViajeRepository viajeRepository;

    // 1) Rutas disponibles, para llenar los selects de origen/destino
    @GetMapping("/rutas")
    public List<Map<String, String>> listarRutas() {
        return viajeRepository.findAll().stream()
                .map(v -> Map.of("origen", v.getOrigen(), "destino", v.getDestino()))
                .distinct()
                .collect(Collectors.toList());
    }

    // 2) Disponibilidad para una ruta y fecha: devuelve UN solo viaje (el que
    //    tenga cupos), no una lista de horarios para elegir. Si hay varios
    //    viajes programados ese día (por ejemplo porque el primero ya se llenó),
    //    se toma el primero que aún tenga cupos disponibles.
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> disponibilidad(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam String fecha) {

        LocalDate fechaViaje = LocalDate.parse(fecha);

        List<Viaje> viajes = viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fechaViaje);

        Viaje viajeConCupos = viajes.stream()
                .filter(v -> v.getEstadoViaje() == EstadoViaje.PROGRAMADO)
                .filter(v -> {
                    int ocupados = pasajeService.asientosOcupados(v).size();
                    return ocupados < v.getTotalAsientos();
                })
                .min(Comparator.comparing(Viaje::getId))
                .orElse(null);

        if (viajeConCupos == null) {
            // No hay viaje disponible ese día: el frontend debe mostrar
            // "no hay disponibilidad, prueba otra fecha".
            return ResponseEntity.ok(null);
        }

        int ocupados = pasajeService.asientosOcupados(viajeConCupos).size();
        int total = viajeConCupos.getTotalAsientos();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("viajeId", viajeConCupos.getId());
        respuesta.put("totalAsientos", total);
        respuesta.put("ocupados", ocupados);
        respuesta.put("disponibles", total - ocupados);
        respuesta.put("precio", viajeConCupos.getPrecio());
        // Se informa la hora estimada, pero el cliente no la elige.
        respuesta.put("horaEstimada", viajeConCupos.getHoraSalida());

        return ResponseEntity.ok(respuesta);
    }

    // 3) Confirmar compra: crea un pasaje por cada pasajero, sin asignar asiento
    @PostMapping("/{viajeId}/reservar-multiples")
    public ResponseEntity<Map<String, Object>> reservarMultiples(
            @PathVariable Long viajeId,
            @RequestBody List<PasajeroReservaDTO> pasajeros,
            Authentication authentication) {

        String email = authentication.getName();
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + viajeId));

        List<Pasaje> creados = pasajeService.reservarMultiples(email, viaje, pasajeros);

        List<String> codigos = creados.stream().map(Pasaje::getCodigoBoleto).toList();
        return ResponseEntity.ok(Map.of("codigos", codigos));
    }

    // 4) Historial de viajes del usuario autenticado
    @GetMapping("/mis")
    public List<PasajeDTO> misViajes(Authentication authentication) {
        String email = authentication.getName();
        return pasajeService.obtenerPasajesDTO(email);
    }

    // 5) Consulta pública de estado por código de boleto
    @GetMapping("/estado/{codigo}")
    public ResponseEntity<Map<String, Object>> estadoPorCodigo(@PathVariable String codigo) {
        PasajeDTO dto = pasajeService.obtenerPasajeDTOPorCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Boleto no encontrado: " + codigo));

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("estado", dto.getEstado());
        respuesta.put("origen", dto.getOrigen());
        respuesta.put("destino", dto.getDestino());
        respuesta.put("fecha", dto.getFechaViaje());
        respuesta.put("horaSalida", dto.getHoraViaje());
        respuesta.put("codigoBoleto", dto.getCodigoBoleto());
        respuesta.put("asiento", dto.getAsiento());
        respuesta.put("nombrePasajero", dto.getNombrePasajero());
        respuesta.put("dniPasajero", dto.getDni());
        respuesta.put("precio", dto.getPrecio());
        respuesta.put("detalles", "Ruta operada por Nueva Ases Express");

        return ResponseEntity.ok(respuesta);
    }
}