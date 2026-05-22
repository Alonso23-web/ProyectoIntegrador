package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;

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
            model.addAttribute("totalPasajes", reservaRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());
            model.addAttribute("totalVehiculos", vehiculoRepository.count());
            return "dashboard/conductor";
        }

        return "dashboard/cliente";
    }
}
