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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RegistroController {

    private final IUsuarioService usuarioService;

    // ========================================================================
    // REGISTRO DE CLIENTES
    // ========================================================================

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
            Model model
    ) {
        // Validaciones
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono));
            return "registro";
        }

        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono));
            return "registro";
        }

        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono));
            return "registro";
        }

        if (usuarioService.existeDni(dni)) {
            model.addAttribute("error", "El DNI ya está registrado.");
            model.addAttribute("usuario", construirUsuarioTemporal(email, nombreCompleto, dni, telefono));
            return "registro";
        }

        UsuarioDTO usuario = UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol("CLIENTE")
                .activo(true)
                .build();

        usuarioService.registrar(usuario, password);

        model.addAttribute("exito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
        return "login";
    }

    // ========================================================================
    // POSTULACIÓN DE CONDUCTORES
    // ========================================================================

    @GetMapping("/postular-conductor")
    public String mostrarFormularioPostulacion(Model model) {
        model.addAttribute("postulacion", new UsuarioDTO());
        return "postulacion-conductor";
    }

    @PostMapping("/postular-conductor")
    public String postular(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            @RequestParam String nombreCompleto,
            @RequestParam String dni,
            @RequestParam String telefono,
            @RequestParam String numeroLicencia,
            @RequestParam Integer aniosExperiencia,
            @RequestParam String tipoVehiculo,
            @RequestParam(required = false) MultipartFile documento,
            Model model
    ) {
        // Validaciones generales
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        if (usuarioService.existeDni(dni)) {
            model.addAttribute("error", "El DNI ya está registrado.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        // Validaciones de conductor
        if (numeroLicencia == null || numeroLicencia.isEmpty()) {
            model.addAttribute("error", "El número de licencia es obligatorio.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        if (aniosExperiencia == null) {
            model.addAttribute("error", "Los años de experiencia son obligatorios.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        if (tipoVehiculo == null || tipoVehiculo.isEmpty()) {
            model.addAttribute("error", "El tipo de vehículo es obligatorio.");
            model.addAttribute("postulacion", construirPostulacionTemporal(email, nombreCompleto, dni, telefono,
                    numeroLicencia, aniosExperiencia, tipoVehiculo));
            return "postulacion-conductor";
        }

        String documentoUrl = null;
        if (documento != null && !documento.isEmpty()) {
            documentoUrl = guardarArchivo(documento);
        }

        UsuarioDTO postulacion = UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol("CONDUCTOR")
                .activo(false)
                .numeroLicencia(numeroLicencia)
                .aniosExperiencia(aniosExperiencia)
                .tipoVehiculo(tipoVehiculo)
                .estadoPostulacion("PENDIENTE")
                .documentoUrl(documentoUrl)
                .build();

        usuarioService.registrar(postulacion, password);

        return "redirect:/postulacion-enviada";
    }

    // ========================================================================
    // MÉTODOS PRIVADOS
    // ========================================================================

    private String guardarArchivo(MultipartFile archivo) {
        try {
            String uploadDir = "src/main/resources/static/uploads/documentos/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path rutaArchivo = uploadPath.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/documentos/" + nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    @GetMapping("/postulacion-enviada")
    public String postulacionEnviada() {
        return "postulacion-enviada";
    }

    private UsuarioDTO construirUsuarioTemporal(String email, String nombreCompleto, String dni, String telefono) {
        return UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol("CLIENTE")
                .build();
    }

    private UsuarioDTO construirPostulacionTemporal(String email, String nombreCompleto, String dni, String telefono,
            String numeroLicencia, Integer aniosExperiencia, String tipoVehiculo) {
        return UsuarioDTO.builder()
                .email(email)
                .nombreCompleto(nombreCompleto)
                .dni(dni)
                .telefono(telefono)
                .rol("CONDUCTOR")
                .numeroLicencia(numeroLicencia)
                .aniosExperiencia(aniosExperiencia)
                .tipoVehiculo(tipoVehiculo)
                .build();
    }
}
