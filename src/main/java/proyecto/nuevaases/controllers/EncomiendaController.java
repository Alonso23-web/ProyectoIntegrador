package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.services.EncomiendaService;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/encomiendas")
@RequiredArgsConstructor
public class EncomiendaController {

    private final EncomiendaService encomiendaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("encomiendas", encomiendaService.listarTodos());
        return "encomiendas/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Encomienda e = new Encomienda();
        e.setFechaEnvio(LocalDate.now());
        e.setEstado("REGISTRADO");
        e.setCodigoRastreo("NAE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        model.addAttribute("encomienda", e);
        return "encomiendas/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("encomienda", encomiendaService.obtenerPorId(id).orElseThrow());
        return "encomiendas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Encomienda encomienda) {
        if (encomienda.getCodigoRastreo() == null || encomienda.getCodigoRastreo().isEmpty()) {
            encomienda.setCodigoRastreo("NAE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (encomienda.getFechaEnvio() == null) {
            encomienda.setFechaEnvio(LocalDate.now());
        }
        if (encomienda.getEstado() == null || encomienda.getEstado().isEmpty()) {
            encomienda.setEstado("REGISTRADO");
        }
        encomiendaService.guardar(encomienda);
        return "redirect:/encomiendas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        encomiendaService.eliminar(id);
        return "redirect:/encomiendas";
    }
}

