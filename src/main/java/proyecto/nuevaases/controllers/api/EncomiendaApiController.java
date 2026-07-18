package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.dto.EncomiendaDTO;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.repositories.ViajeRepository;
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
    private final ViajeRepository viajeRepository;

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
            // Si el creadoPorEmail ya viene en el body (ej: admin registrando para un cliente),
            // respetarlo. Si no, asignar el email del usuario autenticado.
            if (encomienda.getCreadoPorEmail() == null || encomienda.getCreadoPorEmail().isBlank()) {
                encomienda.setCreadoPorEmail(email);
            }
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
        // Guardamos peso/precio en 0; el admin asigna el peso y puede ajustar el precio
        // final.
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

    @GetMapping("/precio-por-peso")
    public ResponseEntity<?> calcularPrecioPorPeso(@RequestParam double peso) {
        double precio = encomiendaService.calcularPrecioPorPeso(peso);
        return ResponseEntity.ok(Map.of(
                "peso", peso,
                "precioPorPeso", precio));
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

    @GetMapping("/viajes-disponibles")
    public ResponseEntity<?> viajesDisponibles() {
        List<Viaje> viajes = viajeRepository.findByFecha(LocalDate.now());
        var resultado = viajes.stream().map(v -> Map.of(
                "id", v.getId(),
                "origen", v.getOrigen(),
                "destino", v.getDestino(),
                "horaSalida", v.getHoraSalida(),
                "conductorEmail", v.getConductorEmail() != null ? v.getConductorEmail() : "",
                "estadoViaje", v.getEstadoViaje().name()
        )).toList();
        return ResponseEntity.ok(resultado);
    }

    @PatchMapping("/{id}/viaje")
    public ResponseEntity<?> asignarViaje(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        EncomiendaDTO enc = encomiendaService.buscarPorId(id);
        Long viajeId = body.get("viajeId");
        if (viajeId == null || viajeId <= 0) {
            enc.setViajeAsignadoId(null);
        } else {
            enc.setViajeAsignadoId(viajeId);
        }
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
