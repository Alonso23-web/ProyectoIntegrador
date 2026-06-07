package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
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
public class ViajeServiceImpl implements ViajeService, IViajeService {

    private final ViajeRepository viajeRepository;
    private final VehiculoRepository vehiculoRepository;

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
