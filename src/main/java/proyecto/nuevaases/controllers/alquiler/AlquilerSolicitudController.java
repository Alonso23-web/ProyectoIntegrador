package proyecto.nuevaases.controllers.alquiler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.nuevaases.dto.SolicitudAlquilerDTO;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.enums.EstadoVehiculo;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.ISolicitudAlquilerService;

import java.util.*;

@Controller
@RequestMapping("/alquiler")
@RequiredArgsConstructor
public class AlquilerSolicitudController {

    private final VehiculoRepository vehiculoRepository;
    private final ISolicitudAlquilerService solicitudAlquilerService;

    @GetMapping
    public String index(
            @RequestParam(required = false) Integer personas,
            Model model
    ) {
        List<Vehiculo> all = vehiculoRepository.findAll();
        List<Vehiculo> filtered = new ArrayList<>();

        for (Vehiculo v : all) {
            if (v.getEstado() == null || v.getEstado() != EstadoVehiculo.DISPONIBLE) {
                continue;
            }
            boolean ok = true;
            if (personas != null && personas > 0) {
                ok = ok && v.getCapacidad() >= personas;
            }
            if (ok) filtered.add(v);
        }

        model.addAttribute("vehiculos", filtered);
        Map<String, Object> param = new HashMap<>();
        param.put("personas", personas);
        model.addAttribute("param", param);

        return "alquiler/index";
    }

    @GetMapping("/vehiculo/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
        model.addAttribute("vehiculo", vehiculo);

        List<String> serviciosIncluidos = List.of(
                "Conductor profesional",
                "Cobertura y asistencia durante el servicio",
                "Mantenimiento preventivo de la unidad"
        );
        model.addAttribute("serviciosIncluidos", serviciosIncluidos);

        return "alquiler/detalle";
    }

    @PostMapping("/solicitud")
    public String enviarSolicitud(
            @RequestParam Map<String, String> params,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String nombre = trim(params.get("nombre"));
        String telefono = trim(params.get("telefono"));
        String correo = trim(params.get("correo"));

        if (nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank() || correo == null || correo.isBlank()) {
            model.addAttribute("error", "Faltan datos obligatorios: nombre, teléfono y correo.");
            return "alquiler/confirmacion";
        }

        // Construir DTO y guardar en BD
        SolicitudAlquilerDTO dto = SolicitudAlquilerDTO.builder()
                .nombreSolicitante(nombre)
                .empresa(trim(params.get("empresa")))
                .telefono(telefono)
                .correo(correo)
                .tipoVehiculo(trim(params.get("tipo")))
                .fechaInicio(parseDate(params.get("fechaInicio")))
                .fechaFin(parseDate(params.get("fechaFin")))
                .cantidadPersonas(parseInt(params.get("personas")))
                .horasPorDia(parseInt(params.get("horasPorDia")))
                .origen(trim(params.get("origen")))
                .destino(trim(params.get("destino")))
                .mensaje(trim(params.get("mensaje")))
                .precioReferencial(parseDouble(params.get("precioReferencial")))
                .build();

        // Asignar vehículo si se envió el ID
        String vehiculoIdStr = params.get("vehiculoId");
        if (vehiculoIdStr != null && !vehiculoIdStr.isBlank()) {
            try {
                dto.setVehiculoId(Long.parseLong(vehiculoIdStr));
            } catch (NumberFormatException ignored) {}
        }

        SolicitudAlquilerDTO guardada = solicitudAlquilerService.guardar(dto);

        redirectAttributes.addFlashAttribute("solicitud", guardada);
        redirectAttributes.addFlashAttribute("enviado", true);
        redirectAttributes.addFlashAttribute("nombre", nombre);

        return "redirect:/alquiler/confirmacion";
    }

    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "alquiler/confirmacion";
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
