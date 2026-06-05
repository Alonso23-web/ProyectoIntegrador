package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.dto.EncomiendaDTO;
import proyecto.nuevaases.services.IEncomiendaService;
import proyecto.nuevaases.services.IUsuarioService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/encomiendas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EncomiendaApiController {

    private final IEncomiendaService encomiendaService;
    private final IUsuarioService usuarioService;

    @GetMapping
    public List<EncomiendaDTO> listar() {
        return encomiendaService.listarTodos();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> rastrear(@PathVariable String codigo) {
        return encomiendaService.buscarPorCodigoRastreo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody EncomiendaDTO encomienda, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            var email = authentication.getName();
            encomienda.setCreadoPorEmail(email);
            usuarioService.buscarPorEmail(email).ifPresent(usuario -> {
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

        // Flujo solicitado: el cliente registra sin pesaje.
        // Guardamos peso/precio en 0; el admin asigna el peso luego.
        encomienda.setPeso(0);
        encomienda.setPrecio(0);

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

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        EncomiendaDTO enc = encomiendaService.buscarPorId(id);
        enc.setEstado(body.get("estado"));
        return ResponseEntity.ok(encomiendaService.guardar(enc));
    }

    @PatchMapping("/{id}/precio")
    public ResponseEntity<?> asignarPrecio(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        EncomiendaDTO enc = encomiendaService.buscarPorId(id);
        enc.setPrecio(body.get("precio"));
        return ResponseEntity.ok(encomiendaService.guardar(enc));
    }

    // Admin asigna peso y el backend recalcula el precio real.
    @PatchMapping("/{id}/peso")
    public ResponseEntity<?> asignarPeso(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        EncomiendaDTO enc = encomiendaService.buscarPorId(id);
        Double peso = body.get("peso");
        if (peso == null || peso <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Peso inválido"));
        }
        enc.setPeso(peso);
        double precio = encomiendaService.calcularPrecio(enc.getOrigen(), enc.getDestino(), peso);
        enc.setPrecio(precio);
        return ResponseEntity.ok(encomiendaService.guardar(enc));
    }
}

