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
import java.util.List;

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
            @RequestParam double precio,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) return ResponseEntity.status(401).body("No autenticado");

        Viaje viaje = viajeRepository.findById(viajeId).orElseThrow();
        Reserva reserva = reservaService.reservar(email, viaje, asiento, precio);
        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/mis")
    public ResponseEntity<?> mis(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(reservaService.misReservas(email));
    }

    @GetMapping("/boleto/{codigoBoleto}")
    public ResponseEntity<?> boleto(@PathVariable String codigoBoleto) {
        return reservaService.obtenerPorCodigoBoleto(codigoBoleto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

