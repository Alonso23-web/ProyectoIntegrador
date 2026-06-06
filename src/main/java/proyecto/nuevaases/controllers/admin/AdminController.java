package proyecto.nuevaases.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.services.IUsuarioService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUsuarioService usuarioService;
    private final ReservaRepository reservaRepository;
    private final EncomiendaRepository encomiendaRepository;

    // ==================== USUARIOS ====================

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodosUsuarios());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/estado/{id}")
    public String cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var usuario = usuarioService.cambiarEstadoActivo(id, activo);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Estado de " + usuario.getNombreCompleto() + " actualizado a " + (activo ? "ACTIVO" : "BLOQUEADO"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // ==================== REPORTES ====================

    @GetMapping("/reportes")
    public String reportes(Model model) {
        // Métricas clave
        long totalPasajes = reservaRepository.count();
        double ingresosHistoricos = reservaRepository.sumAllPrecio();
        long totalEncomiendas = encomiendaRepository.count();
        long diasConViajes = reservaRepository.countDistinctViajeFecha();
        double promedioDiario = diasConViajes > 0 ? (double) totalPasajes / diasConViajes : 0;

        model.addAttribute("totalPasajes", totalPasajes);
        model.addAttribute("ingresosHistoricos", ingresosHistoricos);
        model.addAttribute("totalEncomiendas", totalEncomiendas);
        model.addAttribute("promedioDiario", Math.round(promedioDiario * 100.0) / 100.0);

        // Pasajes agrupados por fecha
        model.addAttribute("pasajesPorFecha", reservaRepository.countAndSumByFecha());

        // Encomiendas por estado
        model.addAttribute("encomiendasPorEstado", encomiendaRepository.countGroupByEstado());

        return "admin/reportes";
    }
}
