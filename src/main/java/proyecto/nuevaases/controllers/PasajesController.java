package proyecto.nuevaases.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasajesController {

    /**
     * Sirve la vista principal de pasajes para clientes con tabs:
     * - buscar viaje
     * - mis viajes
     * - estado de viaje
     */
    @GetMapping("/pasajes/cliente")
    public String clientePasajes(
            @RequestParam(name = "tab", required = false, defaultValue = "buscar") String tab,
            Model model) {
        model.addAttribute("activeTab", tab);
        return "pasajes/cliente-pasajes";
    }
}
