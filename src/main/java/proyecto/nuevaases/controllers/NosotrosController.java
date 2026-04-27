package proyecto.nuevaases.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NosotrosController {

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }
}

