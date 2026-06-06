package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.UsuarioDTO;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    List<UsuarioDTO> listarTodos();
    Optional<UsuarioDTO> buscarPorEmail(String email);
    UsuarioDTO registrar(UsuarioDTO usuarioDTO, String password);
    void eliminar(Long id);
    boolean existeEmail(String email);
    boolean existeDni(String dni);

    // ==================== Conductor management ====================
    List<UsuarioDTO> listarConductoresPendientes();
    List<UsuarioDTO> listarConductores();
    long contarConductoresActivos();
    void aprobarConductor(Long id);
    void rechazarConductor(Long id);

    // ==================== User management (admin) ====================
    List<UsuarioDTO> listarTodosUsuarios();
    UsuarioDTO cambiarEstadoActivo(Long id, boolean activo);
}
