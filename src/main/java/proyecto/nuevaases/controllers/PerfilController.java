package proyecto.nuevaases.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.services.UsuarioService;

@Controller
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public String verPerfil(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioService.obtenerPorEmail(email).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreUsuario", email);

        return "perfil";
    }
}

