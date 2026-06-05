package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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
            @RequestParam(required = false) String numeroLicencia,
            @RequestParam(required = false) Integer aniosExperiencia,
            @RequestParam(required = false) String tipoVehiculo,
            @RequestParam(required = false) MultipartFile documento,
            Model model
    ) {
        // Validaciones
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "registro";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "registro";
        }

        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "registro";
        }

        if (usuarioService.existeDni(dni)) {
            model.addAttribute("error", "El DNI ya está registrado.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "registro";
        }

        // Si es conductor, validar campos requeridos
        if ("CONDUCTOR".equals(rol)) {
            if (numeroLicencia == null || numeroLicencia.isEmpty()) {
                model.addAttribute("error", "El número de licencia es obligatorio.");
                model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                        numeroLicencia, aniosExperiencia, tipoVehiculo));
                return "registro";
            }
            if (aniosExperiencia == null) {
                model.addAttribute("error", "Los años de experiencia son obligatorios.");
                model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                        numeroLicencia, aniosExperiencia, tipoVehiculo));
                return "registro";
            }
            if (tipoVehiculo == null || tipoVehiculo.isEmpty()) {
                model.addAttribute("error", "El tipo de vehículo es obligatorio.");
                model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono, rol,
                        numeroLicencia, aniosExperiencia, tipoVehiculo));
                return "registro";
            }
        }

        UsuarioDTO.UsuarioDTOBuilder builder = UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol(rol)
                .activo(!"CONDUCTOR".equals(rol)); // Conductores inician inactivos

        if ("CONDUCTOR".equals(rol)) {
            String documentoUrl = null;
            if (documento != null && !documento.isEmpty()) {
                documentoUrl = documento.getOriginalFilename();
            }
            builder
                .numeroLicencia(numeroLicencia)
                .aniosExperiencia(aniosExperiencia)
                .tipoVehiculo(tipoVehiculo)
                .estadoPostulacion("PENDIENTE")
                .documentoUrl(documentoUrl);
        }

        UsuarioDTO usuario = builder.build();
        usuarioService.registrar(usuario, password);

        // Si es conductor, redirigir a pantalla de postulación exitosa
        if ("CONDUCTOR".equals(rol)) {
            return "redirect:/postulacion-enviada";
        }

        model.addAttribute("exito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
        return "login";
    }

    @GetMapping("/postulacion-enviada")
    public String postulacionEnviada() {
        return "postulacion-enviada";
    }

    private UsuarioDTO construirUsuarioTemporal(String email, String nombreCompleto, String dni, String telefono, String rol,
            String numeroLicencia, Integer aniosExperiencia, String tipoVehiculo) {
        UsuarioDTO.UsuarioDTOBuilder builder = UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol(rol);
        if ("CONDUCTOR".equals(rol)) {
            builder
                .numeroLicencia(numeroLicencia)
                .aniosExperiencia(aniosExperiencia)
                .tipoVehiculo(tipoVehiculo);
        }
        return builder.build();
    }
}

