package proyecto.nuevaases.controllers.alquiler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.repositories.VehiculoRepository;

import java.util.*;
import java.util.Locale;




@Controller
@RequestMapping("/alquiler")
@RequiredArgsConstructor
public class AlquilerSolicitudController {

    private final VehiculoRepository vehiculoRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer personas,
            @RequestParam(required = false) String ubicacion,
            Model model
    ) {
        // Filtrar vehículos disponibles con capacidad >= personas ingresadas
        List<Vehiculo> all = vehiculoRepository.findAll();
        List<Vehiculo> filtered = new ArrayList<>();

        for (Vehiculo v : all) {
            // Solo vehículos con estado DISPONIBLE
            if (v.getEstado() == null || !v.getEstado().equalsIgnoreCase("DISPONIBLE")) {
                continue;
            }

            boolean ok = true;

            // Filtrar por tipo (búsqueda parcial, insensible a mayúsculas)
            if (tipo != null && !tipo.isBlank()) {
                String tipoVeh = safeStr(v.getTipo());
                ok = ok && tipoVeh.toLowerCase(Locale.ROOT).contains(tipo.toLowerCase(Locale.ROOT));
            }

            // Filtrar por capacidad: mostrar vehículos con capacidad >= personas solicitadas
            if (personas != null && personas > 0) {
                ok = ok && v.getCapacidad() >= personas;
            }

            if (ok) filtered.add(v);
        }

        model.addAttribute("vehiculos", filtered);
        // Map.of() NO permite valores null. Como los filtros son opcionales, evitamos nulls.
        Map<String, Object> param = new HashMap<>();
        param.put("tipo", tipo);
        param.put("fechaInicio", fechaInicio);
        param.put("fechaFin", fechaFin);
        param.put("personas", personas);
        param.put("ubicacion", ubicacion);
        model.addAttribute("param", param);


        return "alquiler/index";
    }

    @GetMapping("/vehiculo/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
        model.addAttribute("vehiculo", vehiculo);

        // Servicios incluidos (simulación)
        List<String> serviciosIncluidos = List.of(
                "Conductor profesional",
                "Cobertura y asistencia durante el servicio",
                "Mantenimiento preventivo de la unidad"
        );
        model.addAttribute("serviciosIncluidos", serviciosIncluidos);

        return "alquiler/detalle";
    }

    @PostMapping("/solicitud")
    public String enviarSolicitud(@RequestParam Map<String, String> params, Model model) {
        // Importante: este endpoint recibe desde el formulario muchos campos.
        // Si falta alguno (por ejemplo CSRF, o si llega un tipo no esperado), puede caer en 500.
        // Validamos estrictamente lo mínimo para evitar errores.

        // Simulación: aquí se guardaría en BD.
        // Para no romper el build por falta de entidad/DB, solo validamos mínimos.
        String nombre = trim(params.get("nombre"));
        String telefono = trim(params.get("telefono"));
        String correo = trim(params.get("correo"));

        if (nombre == null || nombre.isBlank() || telefono == null || telefono.isBlank() || correo == null || correo.isBlank()) {
            model.addAttribute("error", "Faltan datos obligatorios.");
            return "alquiler/confirmacion";
        }

        model.addAttribute("enviado", true);
        model.addAttribute("nombre", nombre);
        model.addAttribute("vehiculoId", params.get("vehiculoId"));
        return "alquiler/confirmacion";
    }

    private static String safeStr(String s) {
        return s == null ? "" : s;
    }

    private static Integer safeInt(Integer i) {
        return i;
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}

