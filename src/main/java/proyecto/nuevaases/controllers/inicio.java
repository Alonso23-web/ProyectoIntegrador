package proyecto.nuevaases.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class inicio {

@GetMapping("/")
String holaMundo(){
    return "index";
}
}
