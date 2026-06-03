package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Viaje;
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

    // ========================================================================
    // Implementación de ViajeService (entity-based — usada por ViajeController)
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
        return ViajeDTO.builder()
                .id(entity.getId())
                .origen(entity.getOrigen())
                .destino(entity.getDestino())
                .fecha(entity.getFecha())
                .horaSalida(entity.getHoraSalida())
                .tipoBus(entity.getTipoBus())
                .totalAsientos(entity.getTotalAsientos())
                .precio(entity.getPrecio())
                .creadoPorEmail(entity.getCreadoPorEmail())
                .build();
    }

    private Viaje convertToEntity(ViajeDTO dto) {
        return Viaje.builder()
                .id(dto.getId())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .fecha(dto.getFecha())
                .horaSalida(dto.getHoraSalida())
                .tipoBus(dto.getTipoBus())
                .totalAsientos(dto.getTotalAsientos())
                .precio(dto.getPrecio())
                .creadoPorEmail(dto.getCreadoPorEmail())
                .build();
    }
}
