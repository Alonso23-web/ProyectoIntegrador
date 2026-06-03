package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.services.IUsuarioService;

@Controller
@RequiredArgsConstructor
public class RegistroController {

    private final IUsuarioService usuarioService;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            @RequestParam String nombreCompleto,
            @RequestParam String dni,
            @RequestParam String telefono,
            @RequestParam String rol,
            Model model
    ) {
        // Validaciones
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol));
            return "registro";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol));
            return "registro";
        }

        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol));
            return "registro";
        }

        if (usuarioService.existeDni(dni)) {
            model.addAttribute("error", "El DNI ya está registrado.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol));
            return "registro";
        }

        UsuarioDTO usuario = UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol(rol)
                .build();

        usuarioService.registrar(usuario, password);
        model.addAttribute("exito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
        return "login";
    }

    private UsuarioDTO construirUsuarioTemporal(String email, String nombreCompleto, String dni, String telefono, String rol) {
        return UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol(rol)
                .build();
    }
}

