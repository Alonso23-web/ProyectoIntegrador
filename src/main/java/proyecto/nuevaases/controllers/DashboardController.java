package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IUsuarioService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import proyecto.nuevaases.models.Reserva;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final EncomiendaRepository encomiendaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ViajeRepository viajeRepository;
    private final IUsuarioService usuarioService;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        model.addAttribute("nombreUsuario", email);

        var usuarioOpt = usuarioRepository.findByEmail(email);
        String rol = usuarioOpt.map(u -> u.getRol()).orElse("ADMINISTRADOR");
        model.addAttribute("rolUsuario", rol);

        if (rol.equals("ADMINISTRADOR")) {
            // ==================== ESTADÍSTICAS DEL DÍA ====================
            LocalDate hoy = LocalDate.now();
            long pasajesDelDia = reservaRepository.countByViajeFecha(hoy);
            double ingresosDelDia = reservaRepository.sumPrecioByViajeFecha(hoy);
            long encomiendasActivas = encomiendaRepository.countByEstadoNot("ENTREGADO");
            long conductoresActivos = usuarioService.contarConductoresActivos();

            model.addAttribute("pasajesDelDia", pasajesDelDia);
            model.addAttribute("ingresosDelDia", ingresosDelDia);
            model.addAttribute("encomiendasActivas", encomiendasActivas);
            model.addAttribute("conductoresActivos", conductoresActivos);

            // ==================== CONDUCTORES PENDIENTES ====================
            model.addAttribute("conductoresPendientes", usuarioService.listarConductoresPendientes());

            // ==================== CONDUCTORES (todos) ====================
            model.addAttribute("conductores", usuarioService.listarConductores());

            // ==================== TABLAS RECIENTES ====================
            model.addAttribute("totalPasajes", reservaRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());
            model.addAttribute("ultimosPasajes", reservaRepository.findByViajeFechaOrderByIdDesc(hoy));
            model.addAttribute("ultimasEncomiendas", encomiendaRepository.findTop5ByOrderByFechaEnvioDesc());

            // Nombre del admin desde la BD
            usuarioOpt.ifPresent(u -> model.addAttribute("nombreAdmin", u.getNombreCompleto()));

            // Fecha formateada en español
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new java.util.Locale("es", "PE"));
            model.addAttribute("fechaHoy", hoy.format(formatter));

            return "dashboard/admin";
        }

        if (rol.equals("CLIENTE")) {
            List<String> estados = List.of("RESERVADO", "PAGADO", "FINALIZADO");
            List<Reserva> ultimosPasajes = reservaRepository.findByUsuarioEmailAndEstadoIn(email, estados);
            // Limitar a 5 solo para mostrar
            model.addAttribute("pasajesComprados", ultimosPasajes.size());
            model.addAttribute("encomiendasRegistradas", encomiendaRepository.countByCreadoPorEmail(email));
            model.addAttribute("ultimosPasajes",
                    ultimosPasajes.stream().limit(5).toList());
            model.addAttribute("ultimasEncomiendas",
                    encomiendaRepository.findTop5ByCreadoPorEmailOrderByFechaEnvioDesc(email));
            return "dashboard/cliente";
        }

        if (rol.equals("CONDUCTOR")) {
            var usuario = usuarioOpt.get();
            String conductorEmail = usuario.getEmail();
            model.addAttribute("nombreCompleto", usuario.getNombreCompleto());

            LocalDate hoy = LocalDate.now();

            // ==================== VIAJES ASIGNADOS DE HOY ====================
            var viajesHoy = viajeRepository.findByConductorEmailAndFechaOrderByHoraSalida(conductorEmail, hoy);

            // Por cada viaje, obtener datos completos
            var viajesConPasajeros = viajesHoy.stream()
                .map(v -> {
                    var pasajeros = reservaRepository.findByViajeAndEstadoIn(
                        v, List.of("RESERVADO", "PAGADO", "FINALIZADO", "EN_CURSO"));
                    return new ViajeConPasajerosYLista(v, pasajeros.size(), pasajeros);
                })
                .toList();
            model.addAttribute("viajesConPasajeros", viajesConPasajeros);

            // ==================== ESTADÍSTICAS PROPIAS ====================
            // Total de viajes históricos del conductor
            long totalViajesCond = viajeRepository.countByConductorEmailAndEstadoViaje(conductorEmail, "FINALIZADO");

            // Total de pasajeros transportados (reservas en viajes del conductor con estado FINALIZADO)
            var viajesConductor = viajeRepository.findByConductorEmail(conductorEmail);
            long totalPasajerosCond = viajesConductor.stream()
                .flatMap(v -> reservaRepository.findByViajeAndEstadoIn(v, List.of("FINALIZADO")).stream())
                .count();

            // Total encomiendas entregadas (asociadas al conductor por su email)
            long totalEncomiendasCond = encomiendaRepository.countByCreadoPorEmail(conductorEmail);

            model.addAttribute("totalViajes", totalViajesCond);
            model.addAttribute("totalPasajeros", totalPasajerosCond);
            model.addAttribute("totalEncomiendas", totalEncomiendasCond);

            return "dashboard/conductor";
        }

        return "dashboard/cliente";
    }

    // ==================== INICIAR VIAJE ====================

    @PostMapping("/conductor/viaje/iniciar/{id}")
    public String iniciarViaje(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var viaje = viajeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
            viaje.setEstadoViaje("EN_CURSO");
            viajeRepository.save(viaje);
            redirectAttributes.addFlashAttribute("mensajeExito", "Viaje iniciado correctamente. ¡Buen viaje!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al iniciar viaje: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ==================== APROBACIÓN DE CONDUCTORES ====================

    @PostMapping("/admin/conductores/aprobar/{id}")
    public String aprobarConductor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.aprobarConductor(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Conductor aprobado correctamente. Ya puede iniciar sesión.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al aprobar conductor: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/conductores/rechazar/{id}")
    public String rechazarConductor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.rechazarConductor(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Conductor rechazado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al rechazar conductor: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // Clase interna para pasar datos de viaje con pasajeros a la vista
    public record ViajeConPasajerosYLista(Viaje viaje, int pasajerosCount, List<Reserva> pasajeros) {}

    public record ViajeConPasajeros(Viaje viaje, int pasajerosCount, int encomiendasCount) {}
}
