package proyecto.nuevaases.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "logout", required = false) String logout, Model model) {
        // El parámetro "logout" lo maneja Thymeleaf vía `${param.logout}` en la vista.
        // Evitamos añadir aquí el atributo "exito" para no duplicar el mensaje
        // cuando Spring Security ya agrega el parámetro de query `?logout`.
        return "login";
    }
}
