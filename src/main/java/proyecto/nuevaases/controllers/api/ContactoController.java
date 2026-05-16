package proyecto.nuevaases.controllers.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactoController {

    @GetMapping("/contacto")
    public String mostrarContacto(Model model) {
        // Inicializamos variables para evitar errores en el template
        model.addAttribute("exito", null);
        return "contacto"; 
    }

    @PostMapping("/contacto")
    public String enviarMensaje(
            @RequestParam(name = "nombre", required = false) String nombre, 
            Model model) {
        
        // Lógica de éxito para el formulario
        model.addAttribute("exito", "¡Gracias " + (nombre != null ? nombre : "cliente") + "! Hemos recibido tu mensaje correctamente.");
        return "contacto";
    }
}