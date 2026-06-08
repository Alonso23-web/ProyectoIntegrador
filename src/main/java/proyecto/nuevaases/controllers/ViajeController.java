package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.models.enums.EstadoPostulacion;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.models.enums.Rol;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.IViajeService;

@Controller
@RequestMapping("/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private static final Logger log = LoggerFactory.getLogger(ViajeController.class);
    private final IViajeService viajeService;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("viajes", viajeService.listarTodosDTO());
        return "viajes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        if (!model.containsAttribute("viaje")) {
            ViajeDTO viaje = ViajeDTO.builder()
                    .totalAsientos(24)
                    .precio(25.0)
                    .build();
            model.addAttribute("viaje", viaje);
        }
        cargarDatosAsignacion(model);
        return "viajes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("viaje", viajeService.obtenerPorIdDTO(id)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado")));
        cargarDatosAsignacion(model);
        return "viajes/formulario";
    }

    private void cargarDatosAsignacion(Model model) {
        model.addAttribute("conductores", usuarioRepository.findByRolAndEstadoPostulacion(
                Rol.CONDUCTOR, EstadoPostulacion.APROBADO));
        model.addAttribute("vehiculos", vehiculoRepository.findAll());
        model.addAttribute("estadosViaje", EstadoViaje.values());
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute ViajeDTO viaje, Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        // Validar que origen y destino no sean iguales
        if (viaje.getOrigen() != null && viaje.getDestino() != null
                && viaje.getOrigen().equalsIgnoreCase(viaje.getDestino())) {
            redirectAttributes.addFlashAttribute("error", "El origen y el destino no pueden ser la misma ciudad.");
            redirectAttributes.addFlashAttribute("viaje", viaje);
            String redirectUrl = viaje.getId() != null ? "/viajes/editar/" + viaje.getId() : "/viajes/nuevo";
            return "redirect:" + redirectUrl;
        }

        if (authentication != null && authentication.isAuthenticated()) {
            if (viaje.getCreadoPorEmail() == null || viaje.getCreadoPorEmail().isBlank()) {
                viaje.setCreadoPorEmail(authentication.getName());
            }
        }
        viajeService.guardarDTO(viaje);
        redirectAttributes.addFlashAttribute("success", "Viaje guardado correctamente.");
        return "redirect:/viajes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            viajeService.eliminarDTO(id);
            redirectAttributes.addFlashAttribute("success", "Viaje eliminado correctamente.");
        } catch (Exception e) {
            log.warn("No se pudo eliminar el viaje {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo eliminar el viaje porque tiene reservas asociadas. Elimina primero las reservas.");
        }
        return "redirect:/viajes";
    }
}
