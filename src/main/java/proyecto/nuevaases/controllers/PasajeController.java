package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.services.IPasajeService;
import proyecto.nuevaases.services.IUsuarioService;

@Controller
@RequestMapping("/pasajes")
@RequiredArgsConstructor
public class PasajeController {

    private final IPasajeService pasajeService;
    private final IUsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pasajes", pasajeService.listarTodos());
        return "pasajes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        PasajeDTO pasaje = PasajeDTO.builder()
                .precio(12.00)
                .build();
        model.addAttribute("pasaje", pasaje);
        return "pasajes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pasaje", pasajeService.buscarPorId(id));
        return "pasajes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute PasajeDTO pasaje, Authentication authentication) {
        // Precio fijo de S/ 12.00
        pasaje.setPrecio(12.00);

        if (authentication != null && authentication.isAuthenticated()) {
            var email = authentication.getName();
            pasaje.setCreadoPorEmail(email);
            usuarioService.buscarPorEmail(email).ifPresent(usuario -> {
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

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam String estado, RedirectAttributes redirectAttributes) {
        try {
            PasajeDTO pasaje = pasajeService.buscarPorId(id);
            pasaje.setEstado(estado);
            pasajeService.guardar(pasaje);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado actualizado a " + estado);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar estado: " + e.getMessage());
        }
        return "redirect:/pasajes";
    }

    @PostMapping("/estado-viaje/{id}")
    public String cambiarEstadoViaje(@PathVariable Long id, @RequestParam String estadoViaje, RedirectAttributes redirectAttributes) {
        try {
            PasajeDTO pasaje = pasajeService.buscarPorId(id);
            pasaje.setEstadoViaje(estadoViaje);
            pasajeService.guardar(pasaje);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado de viaje actualizado a " + estadoViaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar estado de viaje: " + e.getMessage());
        }
        return "redirect:/pasajes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        pasajeService.eliminar(id);
        return "redirect:/pasajes";
    }
}
