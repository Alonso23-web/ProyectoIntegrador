package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.dto.EncomiendaDTO;
import proyecto.nuevaases.services.IEncomiendaService;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class EncomiendaViewController {

    private final IEncomiendaService encomiendaService;

    @GetMapping("/encomiendas")
    public String encomiendas() {
        return "encomiendas";
    }

    @GetMapping("/encomiendas/listar")
    public String listar(Model model) {
        model.addAttribute("encomiendas", encomiendaService.listarTodos());
        return "encomiendas/listar";
    }

    @GetMapping("/encomiendas/nuevo")
    public String nuevo(Model model) {
        EncomiendaDTO encomienda = EncomiendaDTO.builder()
                .codigoRastreo("NAE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .fechaEnvio(LocalDate.now())
                .estado("REGISTRADO")
                .build();
        model.addAttribute("encomienda", encomienda);
        return "encomiendas/formulario";
    }

    @GetMapping("/encomiendas/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("encomienda", encomiendaService.buscarPorId(id));
        return "encomiendas/formulario";
    }

    @PostMapping("/encomiendas/guardar")
    public String guardar(@ModelAttribute EncomiendaDTO encomienda) {
        // Si es nueva (sin ID), generar código y precio
        if (encomienda.getId() == null) {
            if (encomienda.getCodigoRastreo() == null || encomienda.getCodigoRastreo().isBlank()) {
                encomienda.setCodigoRastreo("NAE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
            if (encomienda.getFechaEnvio() == null) {
                encomienda.setFechaEnvio(LocalDate.now());
            }
            if (encomienda.getEstado() == null) {
                encomienda.setEstado("REGISTRADO");
            }
            if (encomienda.getPrecio() == 0) {
                double precio = encomiendaService.calcularPrecio(
                        encomienda.getOrigen(), encomienda.getDestino(), encomienda.getPeso());
                encomienda.setPrecio(precio);
            }
        } else {
            // IMPORTANTÍSIMO: al editar, preservamos creadoPorEmail.
            // Si no lo enviamos desde el formulario, el DTO llega con creadoPorEmail=null y se pierde el filtro de "Mis registros".
            EncomiendaDTO existente = encomiendaService.buscarPorId(encomienda.getId());
            encomienda.setCreadoPorEmail(existente.getCreadoPorEmail());
        }

        encomiendaService.guardar(encomienda);
        return "redirect:/encomiendas";
    }

    @GetMapping("/encomiendas/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        encomiendaService.eliminar(id);
        return "redirect:/encomiendas";
    }
}