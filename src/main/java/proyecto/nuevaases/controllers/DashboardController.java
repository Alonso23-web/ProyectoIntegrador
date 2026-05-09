package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final PasajeRepository pasajeRepository;
    private final EncomiendaRepository encomiendaRepository;
    private final VehiculoRepository vehiculoRepository;

    @GetMapping("/dashboard")
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
            model.addAttribute("totalUsuarios", usuarioRepository.count());
            model.addAttribute("totalPasajes", pasajeRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());
            model.addAttribute("totalVehiculos", vehiculoRepository.count());
            model.addAttribute("ultimosPasajes", pasajeRepository.findTop5ByOrderByFechaViajeDesc());
            model.addAttribute("ultimasEncomiendas", encomiendaRepository.findTop5ByOrderByFechaEnvioDesc());
            return "dashboard/admin";
        }

        if (rol.equals("CLIENTE")) {
            String dni = usuarioOpt.map(u -> u.getDni()).orElse("");
            model.addAttribute("pasajesComprados", pasajeRepository.countByDni(dni));
            model.addAttribute("encomiendasRegistradas",
                    encomiendaRepository.countByDniRemitenteOrDniDestinatario(dni, dni));
            model.addAttribute("ultimosPasajes", pasajeRepository.findTop5ByDniOrderByFechaViajeDesc(dni));
            model.addAttribute("ultimasEncomiendas",
                    encomiendaRepository.findTop5ByDniRemitenteOrDniDestinatarioOrderByFechaEnvioDesc(dni, dni));
            return "dashboard/cliente";
        }

        if (rol.equals("CONDUCTOR")) {
            model.addAttribute("totalPasajes", pasajeRepository.count());
            model.addAttribute("totalEncomiendas", encomiendaRepository.count());
            model.addAttribute("totalVehiculos", vehiculoRepository.count());
            return "dashboard/conductor";
        }

        return "dashboard/cliente";
    }
}
