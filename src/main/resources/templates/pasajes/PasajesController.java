package proyecto.nuevaases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pasajes")
public class PasajesController {

    @GetMapping
    public String pasajesHome(@RequestParam(value = "tab", defaultValue = "buscar") String tab, Model model) {
        model.addAttribute("activeTab", tab);
        return "pasajes/cliente-pasajes";
    }

    @GetMapping("/nuevo")
    public String comprarPasaje(Model model) {
        model.addAttribute("activeTab", "buscar");
        return "pasajes/cliente-pasajes";
    }

    // El estado de viaje se manejará dentro de cliente-pasajes.html
}