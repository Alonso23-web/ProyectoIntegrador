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
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IPasajeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints REST usados por cliente-buscar.js.
 * Muestra TODOS los viajes disponibles para la ruta y fecha seleccionada,
 * con su conductor asignado. El cliente elige cuál le conviene.
 */
@RestController
@RequestMapping("/api/pasajes")
@RequiredArgsConstructor
public class PasajeRestController {

    private final IPasajeService pasajeService;
    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;

    // 1) Rutas disponibles, para llenar los selects de origen/destino
    @GetMapping("/rutas")
    public List<Map<String, String>> listarRutas() {
        return viajeRepository.findAll().stream()
                .map(v -> Map.of("origen", v.getOrigen(), "destino", v.getDestino()))
                .distinct()
                .collect(Collectors.toList());
    }

    // 2) Disponibilidad para una ruta y fecha:
    //    Devuelve TODOS los viajes PROGRAMADOS con cupos disponibles,
    //    cada uno con su información incluyendo el conductor asignado.
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> disponibilidad(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam String fecha) {

        LocalDate fechaViaje = LocalDate.parse(fecha);

        List<Viaje> viajes = viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fechaViaje);

        List<Map<String, Object>> viajesDisponibles = new ArrayList<>();

        for (Viaje v : viajes) {
            if (v.getEstadoViaje() != EstadoViaje.PROGRAMADO) continue;

            int ocupados = pasajeService.asientosOcupados(v).size();
            int disponibles = v.getTotalAsientos() - ocupados;
            if (disponibles <= 0) continue;

            Map<String, Object> viajeInfo = new HashMap<>();
            viajeInfo.put("viajeId", v.getId());
            viajeInfo.put("horaSalida", v.getHoraSalida());
            viajeInfo.put("tipoBus", v.getTipoBus());
            viajeInfo.put("totalAsientos", v.getTotalAsientos());
            viajeInfo.put("ocupados", ocupados);
            viajeInfo.put("disponibles", disponibles);
            viajeInfo.put("precio", v.getPrecio());

            // Buscar nombre del conductor si está asignado
            if (v.getConductorEmail() != null && !v.getConductorEmail().isBlank()) {
                viajeInfo.put("conductorEmail", v.getConductorEmail());
                usuarioRepository.findByEmail(v.getConductorEmail())
                        .ifPresent(u -> viajeInfo.put("conductorNombre", u.getNombreCompleto()));
            } else {
                viajeInfo.put("conductorEmail", "");
                viajeInfo.put("conductorNombre", "Por asignar");
            }

            viajesDisponibles.add(viajeInfo);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("hayDisponibilidad", !viajesDisponibles.isEmpty());
        respuesta.put("viajes", viajesDisponibles);

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
        respuesta.put("estadoViaje", dto.getEstadoViaje());
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