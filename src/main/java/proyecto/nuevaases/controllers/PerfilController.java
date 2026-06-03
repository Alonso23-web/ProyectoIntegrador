package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.services.IUsuarioService;

@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final IUsuarioService usuarioService;

    @GetMapping("/perfil")
    public String verPerfil(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreUsuario", email);

        return "perfil";
    }
}

