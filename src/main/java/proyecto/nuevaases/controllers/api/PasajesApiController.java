package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.ReservaService;
import proyecto.nuevaases.services.ViajeService;

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

    private final ViajeService viajeService;
    private final ReservaService reservaService;
    private final ViajeRepository viajeRepository;

    @GetMapping("/buscar")
    public ResponseEntity<List<Viaje>> buscar(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false, defaultValue = "1") Integer cantidadPasajeros
    ) {
        return ResponseEntity.ok(viajeService.buscar(origen, destino, fecha, cantidadPasajeros));
    }

    @GetMapping("/{viajeId}/ocupacion")
    public ResponseEntity<List<Integer>> ocupacion(@PathVariable Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId).orElseThrow();
        return ResponseEntity.ok(reservaService.asientosOcupados(viaje));
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

        Viaje viaje = viajeRepository.findById(viajeId).orElseThrow();
        Reserva reserva = reservaService.reservar(email, viaje, asiento, nombrePasajero, dniPasajero);
        return ResponseEntity.ok(reserva);
    }

    public record PasajeroReservaDTO(
            String nombrePasajero,
            String dniPasajero,
            int asiento
    ) {}

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

        // Reserva N asientos (cada pasajero trae asiento)
        List<Reserva> reservas = reservaService.reservarMultiples(
                email,
                viaje,
                pasajeros
        );

        return ResponseEntity.ok(reservas);
    }


    @GetMapping("/mis")
    public ResponseEntity<?> mis(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(reservaService.misReservas(email));
    }

    @GetMapping("/estado/{codigoBoleto}")
    public ResponseEntity<Map<String, String>> getEstadoViaje(@PathVariable String codigoBoleto) {
        Optional<Reserva> reservaOpt = reservaService.obtenerPorCodigoBoleto(codigoBoleto);
        if (reservaOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Reserva r = reservaOpt.get();
        Viaje v = r.getViaje();
        String estado = "Programado";
        String detalles = "Su viaje está programado.";

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        
        // Parsear horaSalida que es String en el modelo oficial
        LocalTime horaSalida = LocalTime.parse(v.getHoraSalida());

        if (v.getFecha().isBefore(hoy)) {
            estado = "Finalizado";
            detalles = "El viaje ha concluido.";
        } else if (v.getFecha().isEqual(hoy)) {
            if (ahora.isAfter(horaSalida)) {
                estado = "En ruta";
                detalles = "El bus se encuentra actualmente en trayecto.";
            } else {
                detalles = "El bus sale hoy a las " + v.getHoraSalida();
            }
        } else {
            detalles = "Viaje programado para el " + v.getFecha();
        }

        return ResponseEntity.ok(Map.of("estado", estado, "detalles", detalles));
    }
}
