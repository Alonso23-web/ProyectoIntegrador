package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.services.IUsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioApiController {

    private final IUsuarioService usuarioService;

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarClientes(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<UsuarioDTO> resultados = usuarioService.buscarClientes(q.trim());
        return ResponseEntity.ok(resultados);
    }
}
