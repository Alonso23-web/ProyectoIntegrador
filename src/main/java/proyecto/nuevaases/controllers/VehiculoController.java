package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.services.VehiculoService;

@Controller
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vehiculos", vehiculoService.listarTodos());
        return "vehiculos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        return "vehiculos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("vehiculo", vehiculoService.obtenerPorId(id).orElseThrow());
        return "vehiculos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Vehiculo vehiculo) {
        vehiculoService.guardar(vehiculo);
        return "redirect:/vehiculos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return "redirect:/vehiculos";
    }
}

