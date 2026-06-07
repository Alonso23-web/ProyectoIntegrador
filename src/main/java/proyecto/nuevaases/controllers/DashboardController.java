package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoEncomienda;
import proyecto.nuevaases.models.enums.EstadoPasaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.models.enums.Rol;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IUsuarioService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final PasajeRepository pasajeRepository;
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
        Rol rol = usuarioOpt.map(u -> u.getRol()).orElse(Rol.ADMINISTRADOR);
        usuarioOpt.ifPresent(u -> model.addAttribute("usuario", u));

        if (rol == Rol.ADMINISTRADOR) {
            // ==================== ESTADÍSTICAS DEL DÍA ====================
            LocalDate hoy = LocalDate.now();
            long pasajesDelDia = pasajeRepository.countByViajeFecha(hoy);
            double ingresosDelDia = pasajeRepository.sumPrecioByViajeFecha(hoy);
            long encomiendasActivas = encomiendaRepository.countByEstadoNot(EstadoEncomienda.ENTREGADO);
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
            model.addAttribute("totalPasajes", pasajeRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());
            model.addAttribute("ultimosPasajes", pasajeRepository.findByViajeFechaOrderByIdDesc(hoy));
            model.addAttribute("ultimasEncomiendas", encomiendaRepository.findTop5ByOrderByFechaEnvioDesc());



            // Fecha formateada en español
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new java.util.Locale("es", "PE"));
            model.addAttribute("fechaHoy", hoy.format(formatter));

            return "dashboard/admin";
        }

        if (rol == Rol.CLIENTE) {
            List<EstadoPasaje> estados = List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO, EstadoPasaje.FINALIZADO);
            List<Pasaje> ultimosPasajes = pasajeRepository.findByUsuarioEmailAndEstadoIn(email, estados);
            model.addAttribute("pasajesComprados", ultimosPasajes.size());
            model.addAttribute("encomiendasRegistradas", encomiendaRepository.countByCreadoPorEmail(email));
            model.addAttribute("ultimosPasajes",
                    ultimosPasajes.stream().limit(5).toList());
            model.addAttribute("ultimasEncomiendas",
                    encomiendaRepository.findTop5ByCreadoPorEmailOrderByFechaEnvioDesc(email));
            return "dashboard/cliente";
        }

        if (rol == Rol.CONDUCTOR) {
            var usuario = usuarioOpt.get();
            String conductorEmail = usuario.getEmail();
            LocalDate hoy = LocalDate.now();

            // ==================== VIAJES ASIGNADOS DE HOY ====================
            var viajesHoy = viajeRepository.findByConductorEmailAndFechaOrderByHoraSalida(conductorEmail, hoy);

            // Por cada viaje, obtener datos completos
            var viajesConPasajeros = viajesHoy.stream()
                .map(v -> {
                    var pasajeros = pasajeRepository.findByViajeAndEstadoIn(
                        v, List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO, EstadoPasaje.FINALIZADO));
                    return new ViajeConPasajerosYLista(v, pasajeros.size(), pasajeros);
                })
                .toList();
            model.addAttribute("viajesConPasajeros", viajesConPasajeros);

            // ==================== ESTADÍSTICAS PROPIAS ====================
            long totalViajesCond = viajeRepository.countByConductorEmailAndEstadoViaje(conductorEmail, EstadoViaje.FINALIZADO);

            var viajesConductor = viajeRepository.findByConductorEmail(conductorEmail);
            long totalPasajerosCond = viajesConductor.stream()
                .flatMap(v -> pasajeRepository.findByViajeAndEstadoIn(v, List.of(EstadoPasaje.FINALIZADO)).stream())
                .count();

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
            viaje.setEstadoViaje(EstadoViaje.EN_CURSO);
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
    public record ViajeConPasajerosYLista(Viaje viaje, int pasajerosCount, List<Pasaje> pasajeros) {}

    public record ViajeConPasajeros(Viaje viaje, int pasajerosCount, int encomiendasCount) {}
}
