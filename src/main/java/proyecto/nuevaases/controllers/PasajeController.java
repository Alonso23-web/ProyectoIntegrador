package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.services.PasajeService;
import proyecto.nuevaases.services.UsuarioService;

@Controller
@RequestMapping("/pasajes")
@RequiredArgsConstructor
public class PasajeController {

    private final PasajeService pasajeService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pasajes", pasajeService.listarTodos());
        return "pasajes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pasaje", new Pasaje());
        return "pasajes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pasaje", pasajeService.obtenerPorId(id).orElseThrow());
        return "pasajes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Pasaje pasaje, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            var email = authentication.getName();
            pasaje.setCreadoPorEmail(email);
            usuarioService.obtenerPorEmail(email).ifPresent(usuario -> {
                if (pasaje.getNombrePasajero() == null || pasaje.getNombrePasajero().isBlank()) {
                    pasaje.setNombrePasajero(usuario.getNombreCompleto());
                }
                if (pasaje.getDni() == null || pasaje.getDni().isBlank()) {
                    pasaje.setDni(usuario.getDni());
                }
            });
        }
        if (pasaje.getOrigen() == null || pasaje.getOrigen().isEmpty()) {
            pasaje.setOrigen("Trujillo");
        }
        if (pasaje.getDestino() == null || pasaje.getDestino().isEmpty()) {
            pasaje.setDestino("Chepén");
        }
        pasajeService.guardar(pasaje);
        return "redirect:/pasajes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        pasajeService.eliminar(id);
        return "redirect:/pasajes";
    }
}
