package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.services.IPasajeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasajeServiceImpl implements IPasajeService {

    private final PasajeRepository pasajeRepository;

    @Override
    public List<PasajeDTO> listarTodos() {
        return pasajeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PasajeDTO buscarPorId(Long id) {
        Pasaje pasaje = pasajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pasaje no encontrado con ID: " + id));
        return convertToDTO(pasaje);
    }

    @Override
    public PasajeDTO guardar(PasajeDTO dto) {
        Pasaje pasaje = convertToEntity(dto);
        return convertToDTO(pasajeRepository.save(pasaje));
    }

    @Override
    public void eliminar(Long id) {
        if (!pasajeRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar el pasaje con ID: " + id);
        }
        pasajeRepository.deleteById(id);
    }

    private PasajeDTO convertToDTO(Pasaje entity) {
        return PasajeDTO.builder()
                .id(entity.getId())
                .nombrePasajero(entity.getNombrePasajero())
                .dni(entity.getDni())
                .origen(entity.getOrigen())
                .destino(entity.getDestino())
                .fechaViaje(entity.getFechaViaje())
                .horaViaje(entity.getHoraViaje())
                .asiento(entity.getAsiento())
                .precio(entity.getPrecio())
                .estado(entity.getEstado())
                .creadoPorEmail(entity.getCreadoPorEmail())
                .build();
    }

    private Pasaje convertToEntity(PasajeDTO dto) {
        return Pasaje.builder()
                .id(dto.getId())
                .nombrePasajero(dto.getNombrePasajero())
                .dni(dto.getDni())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .fechaViaje(dto.getFechaViaje())
                .horaViaje(dto.getHoraViaje())
                .asiento(dto.getAsiento())
                .precio(dto.getPrecio())
                .estado(dto.getEstado())
                .creadoPorEmail(dto.getCreadoPorEmail())
                .build();
    }
}