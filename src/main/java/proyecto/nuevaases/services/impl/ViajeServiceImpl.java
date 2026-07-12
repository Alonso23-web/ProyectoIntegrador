package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IViajeService;
import proyecto.nuevaases.services.ViajeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViajeServiceImpl implements ViajeService, IViajeService {

    private final ViajeRepository viajeRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasajeRepository pasajeRepository;

    // ========================================================================
    // Implementación de ViajeService (entity-based)
    // ========================================================================

    @Override
    public List<Viaje> buscar(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros) {
        return viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fecha)
                .stream()
                .filter(v -> v.getTotalAsientos() >= (cantidadPasajeros != null ? cantidadPasajeros : 1))
                .toList();
    }

    @Override
    public Optional<Viaje> obtenerPorId(Long id) {
        return viajeRepository.findById(id);
    }

    @Override
    public List<Viaje> listarTodos() {
        return viajeRepository.findAll();
    }

    @Override
    public void guardar(Viaje viaje) {
        viajeRepository.save(viaje);
    }

    @Override
    public void eliminar(Long id) {
        viajeRepository.deleteById(id);
    }

    // ========================================================================
    // Implementación de IViajeService (DTO-based)
    // ========================================================================

    @Override
    public List<ViajeDTO> listarTodosDTO() {
        return viajeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ViajeDTO> obtenerPorIdDTO(Long id) {
        return viajeRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public ViajeDTO guardarDTO(ViajeDTO dto) {
        Viaje entity = convertToEntity(dto);
        return convertToDTO(viajeRepository.save(entity));
    }

    @Override
    public void eliminarDTO(Long id) {
        if (!viajeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Viaje no encontrado con ID: " + id);
        }
        viajeRepository.deleteById(id);
    }

    @Override
    public List<ViajeDTO> buscarDTO(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros) {
        return buscar(origen, destino, fecha, cantidadPasajeros).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int generarMasivo(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String origen,
            String destino,
            boolean generarInverso,
            List<String> horarios,
            String tipoBus,
            int totalAsientos,
            double precio,
            String creadoPorEmail,
            String conductorEmail,
            Long vehiculoId,
            String estadoViaje
    ) {
        int creados = 0;

        EstadoViaje estado = estadoViaje != null ? EstadoViaje.valueOf(estadoViaje) : EstadoViaje.PROGRAMADO;

        // Recorrer cada día del rango
        LocalDate fecha = fechaInicio;
        while (!fecha.isAfter(fechaFin)) {
            for (String hora : horarios) {
                // Crear viaje ida (origen → destino)
                if (crearSiNoExiste(fecha, hora, origen, destino, tipoBus, totalAsientos, precio,
                        creadoPorEmail, conductorEmail, vehiculoId, estado)) {
                    creados++;
                }

                // Crear viaje vuelta (destino → origen) si se solicitó
                if (generarInverso) {
                    if (crearSiNoExiste(fecha, hora, destino, origen, tipoBus, totalAsientos, precio,
                            creadoPorEmail, conductorEmail, vehiculoId, estado)) {
                        creados++;
                    }
                }
            }
            fecha = fecha.plusDays(1);
        }

        return creados;
    }

    /**
     * Crea un viaje solo si no existe ya uno con la misma ruta, fecha y hora.
     */
    private boolean crearSiNoExiste(
            LocalDate fecha, String hora, String origen, String destino,
            String tipoBus, int totalAsientos, double precio,
            String creadoPorEmail, String conductorEmail, Long vehiculoId,
            EstadoViaje estado) {

        // Verificar si ya existe un viaje con esa ruta + fecha + hora
        boolean existe = viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fecha)
                .stream()
                .anyMatch(v -> v.getHoraSalida().equals(hora));

        if (existe) {
            return false;
        }

        Viaje.ViajeBuilder builder = Viaje.builder()
                .origen(origen)
                .destino(destino)
                .fecha(fecha)
                .horaSalida(hora)
                .tipoBus(tipoBus)
                .totalAsientos(totalAsientos)
                .precio(precio)
                .creadoPorEmail(creadoPorEmail)
                .estadoViaje(estado);

        // Asignar conductor si se especificó
        if (conductorEmail != null && !conductorEmail.isBlank()) {
            builder.conductorEmail(conductorEmail);
        }

        // Asignar vehículo si se especificó
        if (vehiculoId != null) {
            vehiculoRepository.findById(vehiculoId).ifPresent(builder::vehiculo);
        }

        viajeRepository.save(builder.build());
        return true;
    }

    private ViajeDTO convertToDTO(Viaje entity) {
        ViajeDTO.ViajeDTOBuilder builder = ViajeDTO.builder()
                .id(entity.getId())
                .origen(entity.getOrigen())
                .destino(entity.getDestino())
                .fecha(entity.getFecha())
                .horaSalida(entity.getHoraSalida())
                .tipoBus(entity.getTipoBus())
                .totalAsientos(entity.getTotalAsientos())
                .precio(entity.getPrecio())
                .creadoPorEmail(entity.getCreadoPorEmail())
                .conductorEmail(entity.getConductorEmail())
                .estadoViaje(entity.getEstadoViaje().name());

        if (entity.getVehiculo() != null) {
            Vehiculo v = entity.getVehiculo();
            builder.vehiculoId(v.getId());
            builder.vehiculoInfo(v.getMarca() + " " + v.getModelo() + " - " + v.getPlaca());
        }

        return builder.build();
    }

    private Viaje convertToEntity(ViajeDTO dto) {
        Viaje.ViajeBuilder builder = Viaje.builder()
                .id(dto.getId())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .fecha(dto.getFecha())
                .horaSalida(dto.getHoraSalida())
                .tipoBus(dto.getTipoBus())
                .totalAsientos(dto.getTotalAsientos())
                .precio(dto.getPrecio())
                .creadoPorEmail(dto.getCreadoPorEmail())
                .conductorEmail(dto.getConductorEmail())
                .estadoViaje(dto.getEstadoViaje() != null ? EstadoViaje.valueOf(dto.getEstadoViaje()) : EstadoViaje.PROGRAMADO);

        if (dto.getVehiculoId() != null) {
            vehiculoRepository.findById(dto.getVehiculoId()).ifPresent(builder::vehiculo);
        }

        return builder.build();
    }
}
