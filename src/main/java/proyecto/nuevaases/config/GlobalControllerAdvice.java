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
import proyecto.nuevaases.repositories.SolicitudAlquilerRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.ViajeRepository;

import java.time.LocalDate;
import java.util.Optional;

// Inyecta datos en TODOS los controladores (Thymeleaf) sin repetir código
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContactoMensajeRepository contactoMensajeRepository;
    private final SolicitudAlquilerRepository solicitudAlquilerRepository;

    // Este método se ejecuta en cada request y agrega "adminNavData" al modelo
    @ModelAttribute("adminNavData")
    public AdminNavData addAdminNavData(Authentication authentication) {
        // Si el usuario no está autenticado, devuelve datos vacíos (no admin)
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AdminNavData(0, 0, 0, 0, null, null, false);
        }

        String email = authentication.getName();

        // Busca al usuario en BD por su email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        boolean isAdmin = usuarioOpt.map(u -> u.getRol() == Rol.ADMINISTRADOR).orElse(false);

        // Si NO es admin, solo pasa su email y nombre (sin alertas de admin)
        if (!isAdmin) {
            String name = usuarioOpt.map(Usuario::getNombreCompleto).orElse(email);
            return new AdminNavData(0, 0, 0, 0, email, name, false);
        }

        // Si ES admin: cuenta viajes en curso, conductores pendientes, mensajes no leídos y solicitudes de alquiler pendientes
        long viajesEnCurso = viajeRepository.countByEstadoViajeAndFecha(EstadoViaje.EN_CURSO, LocalDate.now());
        long conductoresPendientes = usuarioRepository.countByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.PENDIENTE);
        long mensajesNoLeidos = contactoMensajeRepository.countByLeido(false);
        long solicitudesPendientes = solicitudAlquilerRepository.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE").size();

        String nombre = usuarioOpt.map(Usuario::getNombreCompleto).orElse(email);

        return new AdminNavData((int) viajesEnCurso, (int) conductoresPendientes, (int) mensajesNoLeidos, (int) solicitudesPendientes, email, nombre, true);
    }

    // Record (DTO inmutable) que se pasa al navbar de todas las vistas
    public record AdminNavData(
        int viajesEnCurso,
        int conductoresPendientes,
        int mensajesNoLeidos,
        int solicitudesPendientes,
        String email,
        String nombreCompleto,
        boolean esAdmin
    ) {
        // Suma las alertas más importantes para mostrar un badge en el navbar
        public int totalAlertas() {
            return viajesEnCurso + conductoresPendientes + solicitudesPendientes;
        }
    }
}
