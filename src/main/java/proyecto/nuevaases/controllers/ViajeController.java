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
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.IViajeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private static final Logger log = LoggerFactory.getLogger(ViajeController.class);
    private final IViajeService viajeService;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasajeRepository pasajeRepository;

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

    // ========================================================================
    // GENERACIÓN MASIVA DE VIAJES
    // ========================================================================

    @GetMapping("/generar-masivo")
    public String generarMasivoForm(Model model) {
        cargarDatosAsignacion(model);
        return "viajes/generar-masivo";
    }

    @PostMapping("/generar-masivo")
    public String generarMasivo(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin,
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam(defaultValue = "false") boolean generarInverso,
            @RequestParam(required = false) List<String> horarios,
            @RequestParam String tipoBus,
            @RequestParam int totalAsientos,
            @RequestParam double precio,
            @RequestParam(required = false) String conductorEmail,
            @RequestParam(required = false) Long vehiculoId,
            @RequestParam(defaultValue = "PROGRAMADO") String estadoViaje,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Validar rango
        if (fechaInicio.isAfter(fechaFin)) {
            redirectAttributes.addFlashAttribute("error", "La fecha de inicio no puede ser posterior a la fecha fin.");
            return "redirect:/viajes/generar-masivo";
        }

        // Validar origen ≠ destino
        if (origen.equalsIgnoreCase(destino)) {
            redirectAttributes.addFlashAttribute("error", "El origen y el destino no pueden ser la misma ciudad.");
            return "redirect:/viajes/generar-masivo";
        }

        // Validar al menos un horario
        if (horarios == null || horarios.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar al menos un horario.");
            return "redirect:/viajes/generar-masivo";
        }

        String email = authentication != null ? authentication.getName() : "admin@empresa.com";

        int creados = viajeService.generarMasivo(
                fechaInicio, fechaFin, origen, destino, generarInverso,
                horarios, tipoBus, totalAsientos, precio, email,
                conductorEmail, vehiculoId, estadoViaje
        );

        redirectAttributes.addFlashAttribute("success",
                "✅ Se crearon " + creados + " viaje(s) exitosamente. " +
                (creados == 0 ? "(puede que ya existieran viajes para esas fechas y horarios)" : ""));
        return "redirect:/viajes";
    }

    // ========================================================================
    // ELIMINACIÓN MASIVA DE VIAJES
    // ========================================================================

    @PostMapping("/eliminar-masivo")
    public String eliminarMasivo(@RequestParam(required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se seleccionaron viajes para eliminar.");
            return "redirect:/viajes";
        }

        int eliminados = 0;
        int errores = 0;
        for (Long id : ids) {
            try {
                viajeService.eliminarDTO(id);
                eliminados++;
            } catch (Exception e) {
                log.warn("No se pudo eliminar el viaje {}: {}", id, e.getMessage());
                errores++;
            }
        }

        String mensaje = "✅ Se eliminaron " + eliminados + " viaje(s).";
        if (errores > 0) {
            mensaje += " ⚠️ " + errores + " viaje(s) no se pudieron eliminar (tienen reservas u otros impedimentos).";
        }
        redirectAttributes.addFlashAttribute("success", mensaje);
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
                    "No se pudo eliminar el viaje: " + e.getMessage());
        }
        return "redirect:/viajes";
    }

    // ========================================================================
    // API: VERIFICAR RESERVAS (para el modal individual)
    // ========================================================================

    @GetMapping("/api/verificar-reservas/{id}")
    @ResponseBody
    public Map<String, Object> verificarReservas(@PathVariable Long id) {
        long count = pasajeRepository.countByViajeId(id);
        return Map.of("tieneReservas", count > 0, "cantidad", count);
    }

    @GetMapping("/api/verificar-reservas-masivo")
    @ResponseBody
    public Map<String, Object> verificarReservasMasivo(@RequestParam List<Long> ids) {
        long totalConReservas = 0;
        for (Long id : ids) {
            if (pasajeRepository.countByViajeId(id) > 0) {
                totalConReservas++;
            }
        }
        return Map.of("totalConReservas", totalConReservas, "totalSeleccionados", ids.size());
    }
}
