package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.services.IUsuarioService;

@Controller
@RequiredArgsConstructor
public class PasajesController {

    private final IUsuarioService usuarioService;

    /**
     * Sirve la vista principal de pasajes para clientes con tabs:
     * - buscar viaje
     * - mis viajes
     * - estado de viaje
     */
    @GetMapping("/pasajes/cliente")
    public String clientePasajes(
            @RequestParam(name = "tab", required = false, defaultValue = "buscar") String tab,
            Authentication authentication,
            Model model) {
        model.addAttribute("activeTab", tab);

        // Pasar datos del usuario autenticado para autocompletar primer pasajero
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            usuarioService.buscarPorEmail(email).ifPresent(usuario -> 
                model.addAttribute("usuario", usuario)
            );
        }

        return "pasajes/cliente-pasajes";
    }
}
