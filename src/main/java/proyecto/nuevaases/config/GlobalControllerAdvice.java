package proyecto.nuevaases.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import proyecto.nuevaases.models.ContactoMensaje;
import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.models.enums.EstadoPostulacion;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.models.enums.Rol;
import proyecto.nuevaases.repositories.ContactoMensajeRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.ViajeRepository;

import java.time.LocalDate;
import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContactoMensajeRepository contactoMensajeRepository;

    @ModelAttribute("adminNavData")
    public AdminNavData addAdminNavData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AdminNavData(0, 0, 0, null, null, false);
        }

        String email = authentication.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        boolean isAdmin = usuarioOpt.map(u -> u.getRol() == Rol.ADMINISTRADOR).orElse(false);

        if (!isAdmin) {
            String name = usuarioOpt.map(Usuario::getNombreCompleto).orElse(email);
            return new AdminNavData(0, 0, 0, email, name, false);
        }

        long viajesEnCurso = viajeRepository.countByEstadoViajeAndFecha(EstadoViaje.EN_CURSO, LocalDate.now());
        long conductoresPendientes = usuarioRepository.countByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.PENDIENTE);
        long mensajesNoLeidos = contactoMensajeRepository.countByLeido(false);

        String nombre = usuarioOpt.map(Usuario::getNombreCompleto).orElse(email);

        return new AdminNavData((int) viajesEnCurso, (int) conductoresPendientes, (int) mensajesNoLeidos, email, nombre, true);
    }

    public record AdminNavData(
        int viajesEnCurso,
        int conductoresPendientes,
        int mensajesNoLeidos,
        String email,
        String nombreCompleto,
        boolean esAdmin
    ) {
        public int totalAlertas() {
            return viajesEnCurso + conductoresPendientes;
        }
    }
}
