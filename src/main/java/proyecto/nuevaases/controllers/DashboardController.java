package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.repositories.ViajeRepository;

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
            model.addAttribute("totalPasajes", reservaRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());
            model.addAttribute("ultimosPasajes", reservaRepository.findTop5ByOrderByIdDesc());
            model.addAttribute("ultimasEncomiendas", encomiendaRepository.findTop5ByOrderByFechaEnvioDesc());
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
            model.addAttribute("nombreCompleto", usuario.getNombreCompleto());

            // Datos de viajes de hoy
            var viajesHoy = viajeRepository.findByFecha(LocalDate.now());
            model.addAttribute("viajesHoy", viajesHoy);

            // Por cada viaje, contar pasajeros
            var viajesConPasajeros = viajesHoy.stream()
                .map(v -> {
                    var pasajeros = reservaRepository.findByViajeAndEstadoIn(
                        v, List.of("RESERVADO", "PAGADO", "FINALIZADO"));
                    var encomiendas = encomiendaRepository.findByCreadoPorEmail(usuario.getEmail());
                    return new ViajeConPasajeros(v, pasajeros.size(), encomiendas.size());
                })
                .toList();
            model.addAttribute("viajesConPasajeros", viajesConPasajeros);

            // Total pasajeros transportados (reservas en estado FINALIZADO)
            long totalPasajeros = reservaRepository.count();
            model.addAttribute("totalPasajeros", totalPasajeros);

            // Datos generales
            model.addAttribute("totalViajes", viajeRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());

            // Alertas específicas
            boolean hayViajeProximo = viajesHoy.stream()
                .anyMatch(v -> v.getHoraSalida() != null && !v.getHoraSalida().isEmpty());
            model.addAttribute("hayViajeProximo", hayViajeProximo);

            boolean faltanPasajeros = viajesHoy.stream()
                .anyMatch(v -> reservaRepository.findByViajeAndEstadoIn(v, List.of("RESERVADO")).size() > 0);
            model.addAttribute("faltanPasajeros", faltanPasajeros);

            return "dashboard/conductor";
        }

        return "dashboard/cliente";
    }

    // Clase interna para pasar datos de viaje con pasajeros a la vista
    public record ViajeConPasajeros(Viaje viaje, int pasajerosCount, int encomiendasCount) {}
}
