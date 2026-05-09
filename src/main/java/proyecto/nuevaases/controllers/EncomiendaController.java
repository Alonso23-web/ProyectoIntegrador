package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.services.EncomiendaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/encomiendas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EncomiendaController {

    private final EncomiendaService encomiendaService;

    @GetMapping
    public List<Encomienda> listar() {
        return encomiendaService.listarTodos();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> rastrear(@PathVariable String codigo) {
        return encomiendaService.buscarPorCodigoRastreo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Encomienda encomienda) {
        encomienda.setCodigoRastreo("NAE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        encomienda.setFechaEnvio(LocalDate.now());
        encomienda.setEstado("REGISTRADO");
        encomienda.setPrecio(encomiendaService.calcularPrecio(
                encomienda.getOrigen(), encomienda.getDestino(), encomienda.getPeso()));
        return ResponseEntity.ok(encomiendaService.guardar(encomienda));
    }

    @GetMapping("/precio")
    public ResponseEntity<?> calcularPrecio(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam double peso) {
        double precio = encomiendaService.calcularPrecio(origen, destino, peso);
        return ResponseEntity.ok(Map.of(
                "origen", origen,
                "destino", destino,
                "peso", peso,
                "precio", precio));
    }

    @GetMapping("/historial")
    public ResponseEntity<?> historial(
            @RequestParam String dni,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(encomiendaService.buscarPorDni(dni, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        encomiendaService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}