package proyecto.nuevaases.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import proyecto.nuevaases.dto.VehiculoDTO;
import proyecto.nuevaases.services.IVehiculoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final IVehiculoService vehiculoService;

    private static final String UPLOAD_DIR = "./uploads/vehiculos/";

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vehiculos", vehiculoService.listarTodos());
        return "vehiculos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("vehiculo", new VehiculoDTO());
        return "vehiculos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("vehiculo", vehiculoService.buscarPorId(id));
        return "vehiculos/formulario";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("vehiculo", vehiculoService.buscarPorId(id));
        return "vehiculos/detalle";
    }

    @PostMapping(value = "/guardar", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarAjax(
            @RequestParam(required = false) Long id,
            @RequestParam String placa,
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int anio,
            @RequestParam int capacidad,
            @RequestParam String tipo,
            @RequestParam String estado,
            @RequestParam double precioPorDia,
            @RequestParam(required = false) String descripcion,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile) {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        // === Validación del lado del servidor ===
        // Placa
        if (placa == null || placa.trim().isEmpty()) {
            errors.put("placa", "La placa es obligatoria.");
        } else if (!placa.matches("^[A-Za-z0-9-]+$")) {
            errors.put("placa", "La placa solo puede contener letras, números y guiones.");
        }

        // Marca
        if (marca == null || marca.trim().isEmpty()) {
            errors.put("marca", "La marca es obligatoria.");
        }

        // Modelo
        if (modelo == null || modelo.trim().isEmpty()) {
            errors.put("modelo", "El modelo es obligatorio.");
        }

        // Año
        if (anio < 2000 || anio > 2030) {
            errors.put("anio", "El año debe ser entre 2000 y 2030.");
        }

        // Capacidad
        if (capacidad < 1 || capacidad > 50) {
            errors.put("capacidad", "La capacidad debe ser entre 1 y 50.");
        }

        // Tipo
        if (tipo == null || tipo.trim().isEmpty()) {
            errors.put("tipo", "Selecciona un tipo de vehículo.");
        }

        // Estado
        if (estado == null || estado.trim().isEmpty()) {
            errors.put("estado", "Selecciona un estado.");
        }

        // Precio
        if (precioPorDia <= 0) {
            errors.put("precioPorDia", "El precio debe ser mayor a 0.");
        }

        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Guardar imagen si se subió un archivo
            String imagenUrl = null;
            if (imagenFile != null && !imagenFile.isEmpty()) {
                imagenUrl = guardarImagen(imagenFile);
            } else if (id != null) {
                // Mantener imagen existente si no se subió nueva
                VehiculoDTO existente = vehiculoService.buscarPorId(id);
                imagenUrl = existente.getImagen();
            }

            VehiculoDTO dto = VehiculoDTO.builder()
                    .id(id)
                    .placa(placa.trim())
                    .marca(marca.trim())
                    .modelo(modelo.trim())
                    .anio(anio)
                    .capacidad(capacidad)
                    .tipo(tipo)
                    .estado(estado)
                    .precioPorDia(precioPorDia)
                    .imagen(imagenUrl)
                    .descripcion(descripcion != null ? descripcion.trim() : null)
                    .build();

            VehiculoDTO saved = vehiculoService.guardar(dto);
            response.put("success", true);
            response.put("redirectUrl", "/vehiculos");
        } catch (Exception e) {
            errors.put("general", "Error al guardar: " + e.getMessage());
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-estado/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {

        Map<String, Object> response = new HashMap<>();
        try {
            VehiculoDTO vehiculo = vehiculoService.buscarPorId(id);
            vehiculo.setEstado(nuevoEstado);
            vehiculoService.guardar(vehiculo);
            response.put("success", true);
            response.put("message", "Estado actualizado correctamente.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar estado: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return "redirect:/vehiculos";
    }

    private String guardarImagen(MultipartFile file) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueName = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path filePath = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath);

        return "/uploads/vehiculos/" + uniqueName;
    }
}
