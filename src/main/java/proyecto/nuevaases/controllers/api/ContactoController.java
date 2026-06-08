package proyecto.nuevaases.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.nuevaases.services.IContactoMensajeService;

@Controller
@RequiredArgsConstructor
public class ContactoController {

    private final IContactoMensajeService contactoMensajeService;

    @GetMapping("/contacto")
    public String mostrarContacto(Model model) {
        model.addAttribute("exito", null);
        return "contacto";
    }

    @PostMapping("/contacto")
    public String enviarMensaje(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam String mensaje,
            Model model) {

        contactoMensajeService.guardar(nombre, correo, telefono, mensaje);

        model.addAttribute("exito",
                "¡Gracias " + nombre + "! Hemos recibido tu mensaje correctamente. Te responderemos pronto.");
        return "contacto";
    }
}
