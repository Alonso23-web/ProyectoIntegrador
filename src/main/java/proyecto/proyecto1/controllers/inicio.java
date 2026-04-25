package proyecto.proyecto1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class inicio {

@GetMapping("/")
String holaMundo(){
    return "index";
}
}
