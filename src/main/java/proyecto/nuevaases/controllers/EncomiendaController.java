package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.services.EncomiendaService;
import proyecto.nuevaases.services.UsuarioService;

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
    private final UsuarioService usuarioService;

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
    public ResponseEntity<?> registrar(@RequestBody Encomienda encomienda, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            var email = authentication.getName();
            encomienda.setCreadoPorEmail(email);
            usuarioService.obtenerPorEmail(email).ifPresent(usuario -> {
                if (encomienda.getRemitente() == null || encomienda.getRemitente().isBlank()) {
                    encomienda.setRemitente(usuario.getNombreCompleto());
                }
                if (encomienda.getDniRemitente() == null || encomienda.getDniRemitente().isBlank()) {
                    encomienda.setDniRemitente(usuario.getDni());
                }
            });
        }
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

    @GetMapping("/mis-registros")
    public ResponseEntity<?> misRegistros(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            var email = authentication.getName();
            return ResponseEntity.ok(encomiendaService.buscarPorCreadoPorEmail(email));
        }
        return ResponseEntity.status(401).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        encomiendaService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}