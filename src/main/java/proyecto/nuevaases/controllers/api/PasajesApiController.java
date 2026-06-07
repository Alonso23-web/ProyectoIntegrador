package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IPasajeService;
import proyecto.nuevaases.services.IViajeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pasajes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PasajesApiController {

    private final IViajeService viajeService;
    private final IPasajeService pasajeService;
    private final ViajeRepository viajeRepository;

    @GetMapping("/rutas")
    public ResponseEntity<List<Map<String, String>>> rutas() {
        List<ViajeDTO> viajes = viajeService.listarTodosDTO();
        var rutas = viajes.stream()
                .map(v -> Map.of("origen", v.getOrigen(), "destino", v.getDestino()))
                .distinct()
                .toList();
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ViajeDTO>> buscar(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false, defaultValue = "1") Integer cantidadPasajeros
    ) {
        return ResponseEntity.ok(viajeService.buscarDTO(origen, destino, fecha, cantidadPasajeros));
    }

    @GetMapping("/{viajeId}/ocupacion")
    public ResponseEntity<List<Integer>> ocupacion(@PathVariable Long viajeId) {
        return ResponseEntity.ok(pasajeService.asientosOcupados(viajeId));
    }

    @PostMapping("/{viajeId}/reservar")
    public ResponseEntity<?> reservar(
            @PathVariable Long viajeId,
            @RequestParam int asiento,
            @RequestParam String nombrePasajero,
            @RequestParam String dniPasajero,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) return ResponseEntity.status(401).body("No autenticado");

        PasajeDTO pasaje = pasajeService.reservarDTO(email, viajeId, asiento, nombrePasajero, dniPasajero);
        return ResponseEntity.ok(pasaje);
    }

    @PostMapping("/{viajeId}/reservar-multiples")
    public ResponseEntity<?> reservarMultiples(
            @PathVariable Long viajeId,
            @RequestBody List<PasajeroReservaDTO> pasajeros,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) return ResponseEntity.status(401).body("No autenticado");

        if (pasajeros == null || pasajeros.isEmpty()) {
            return ResponseEntity.badRequest().body("Debes indicar al menos 1 pasajero");
        }

        Viaje viaje = viajeRepository.findById(viajeId).orElseThrow();

        var pasajes = pasajeService.reservarMultiples(email, viaje, pasajeros);

        return ResponseEntity.ok(pasajes);
    }


    @GetMapping("/disponibilidad-horaria")
    public ResponseEntity<List<Map<String, Object>>> disponibilidadHoraria(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<Viaje> viajes = viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fecha);
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Viaje v : viajes) {
            List<Integer> ocupados = pasajeService.asientosOcupados(v);
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("hora", v.getHoraSalida());
            item.put("viajeId", v.getId());
            item.put("totalAsientos", v.getTotalAsientos());
            item.put("ocupados", ocupados.size());
            item.put("disponibles", v.getTotalAsientos() - ocupados.size());
            item.put("tipoBus", v.getTipoBus());
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/mis")
    public ResponseEntity<?> mis(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(pasajeService.obtenerPasajesDTO(email));
    }

    @GetMapping("/estado/{codigoBoleto}")
    public ResponseEntity<Map<String, Object>> getEstadoViaje(@PathVariable String codigoBoleto) {
        Optional<PasajeDTO> pasajeOpt = pasajeService.obtenerPasajeDTOPorCodigo(codigoBoleto);
        if (pasajeOpt.isEmpty()) return ResponseEntity.notFound().build();

        PasajeDTO p = pasajeOpt.get();

        // Determinar estado según la fecha/hora actual
        String estado = p.getEstado();
        String detalles = "Su viaje está programado.";

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        try {
            LocalTime horaSalida = LocalTime.parse(p.getHoraViaje());

            if (p.getFechaViaje().isBefore(hoy)) {
                estado = "FINALIZADO";
                detalles = "El viaje ha concluido.";
            } else if (p.getFechaViaje().isEqual(hoy)) {
                if (ahora.isAfter(horaSalida)) {
                    estado = "EN_RUTA";
                    detalles = "El bus se encuentra actualmente en trayecto.";
                } else {
                    detalles = "El bus sale hoy a las " + p.getHoraViaje();
                }
            } else {
                detalles = "Viaje programado para el " + p.getFechaViaje() + " a las " + p.getHoraViaje();
            }
        } catch (Exception e) {
            detalles = "Viaje programado.";
        }

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("codigoBoleto", p.getCodigoBoleto());
        response.put("estado", estado);
        response.put("detalles", detalles);
        response.put("origen", p.getOrigen());
        response.put("destino", p.getDestino());
        response.put("fecha", p.getFechaViaje() != null ? p.getFechaViaje().toString() : "");
        response.put("horaSalida", p.getHoraViaje());
        response.put("tipoBus", "");
        response.put("precio", p.getPrecio());
        response.put("asiento", p.getAsiento());
        response.put("nombrePasajero", p.getNombrePasajero());
        response.put("dniPasajero", p.getDni());

        return ResponseEntity.ok(response);
    }
}
