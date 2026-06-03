package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import proyecto.nuevaases.services.IEncomiendaService;

@Controller
@RequiredArgsConstructor
public class RastrearController {

    private final IEncomiendaService encomiendaService;

    @GetMapping("/rastrear")
    public String rastrear(@RequestParam(required = false) String codigo, Model model) {
        if (codigo != null && !codigo.isEmpty()) {
            encomiendaService.buscarPorCodigoRastreo(codigo)
                    .ifPresentOrElse(
                            encomienda -> model.addAttribute("encomienda", encomienda),
                            () -> model.addAttribute("error", "No se encontró ninguna encomienda con el código: " + codigo)
                    );
            model.addAttribute("codigoBuscado", codigo);
        }
        return "rastrear";
    }
}

