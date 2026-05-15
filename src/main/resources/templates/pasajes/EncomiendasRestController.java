package proyecto.nuevaases.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.model.Encomienda;
import proyecto.nuevaases.repository.EncomiendaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/encomiendas")
public class EncomiendasRestController {

    @Autowired
    private EncomiendaRepository encomiendaRepository;

    // Helper to get current user's DNI (placeholder for actual security context)
    private String getCurrentUserDni() {
        // In a real application, retrieve DNI from authenticated user's principal
        // For now, hardcoding or getting from a dummy user.
        return "87654321"; // Example DNI for a sender
    }

    // Endpoint for registrar()
    @PostMapping
    public ResponseEntity<Encomienda> registrarEncomienda(@RequestBody Encomienda encomienda) {
        // Simulate cost calculation (as done in frontend for consistency)
        double tarifaBase = 10.00;
        double precioKg = 2.50;
        double factorDistancia = (encomienda.getOrigen().equalsIgnoreCase(encomienda.getDestino())) ? 0 : 5.00;
        double costoCalculado = tarifaBase + (encomienda.getPesoEstimado() * precioKg) + factorDistancia;
        encomienda.setCostoEstimado(costoCalculado);

        encomienda.setEstado("REGISTRADO");
        encomienda.setCodigoRastreo("NAE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        encomienda.setFechaEnvio(LocalDate.now());
        // Simulate estimated delivery date (e.g., 3 days from now)
        encomienda.setFechaEstimadaEntrega(LocalDate.now().plusDays(3));

        Encomienda savedEncomienda = encomiendaRepository.save(encomienda);
        return ResponseEntity.ok(savedEncomienda);
    }

    // Endpoint for rastrear()
    @GetMapping("/{codigoRastreo}")
    public ResponseEntity<Encomienda> rastrearEncomienda(@PathVariable String codigoRastreo) {
        Optional<Encomienda> encomienda = encomiendaRepository.findByCodigoRastreo(codigoRastreo);
        return encomienda.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint for cargarMisRegistros()
    @GetMapping("/mis-registros")
    public ResponseEntity<List<Encomienda>> getMisEncomiendas() {
        return ResponseEntity.ok(encomiendaRepository.findByDniRemitente(getCurrentUserDni()));
    }
}