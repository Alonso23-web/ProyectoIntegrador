package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.EncomiendaDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.services.IEncomiendaService;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EncomiendaServiceImpl implements IEncomiendaService {

    private final EncomiendaRepository encomiendaRepository;

    @Override
    public List<EncomiendaDTO> listarTodos() {
        return encomiendaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EncomiendaDTO buscarPorId(Long id) {
        Encomienda entity = encomiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encomienda no encontrada con ID: " + id));
        return convertToDTO(entity);
    }

    @Override
    public EncomiendaDTO guardar(EncomiendaDTO dto) {
        Encomienda entity = convertToEntity(dto);
        if (entity.getCodigoRastreo() == null || entity.getCodigoRastreo().isEmpty()) {
            entity.setCodigoRastreo(generarCodigoRastreo());
        }
        return convertToDTO(encomiendaRepository.save(entity));
    }

    @Override
    public void eliminar(Long id) {
        if (!encomiendaRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar la encomienda con ID: " + id);
        }
        encomiendaRepository.deleteById(id);
    }

    private String generarCodigoRastreo() {
        return "NAE-2024-" + (new Random().nextInt(9000) + 1000);
    }

    private EncomiendaDTO convertToDTO(Encomienda entity) {
        return EncomiendaDTO.builder()
                .id(entity.getId()).codigoRastreo(entity.getCodigoRastreo())
                .remitente(entity.getRemitente()).dniRemitente(entity.getDniRemitente())
                .destinatario(entity.getDestinatario()).dniDestinatario(entity.getDniDestinatario())
                .origen(entity.getOrigen()).destino(entity.getDestino())
                .descripcion(entity.getDescripcion()).peso(entity.getPeso())
                .precio(entity.getPrecio()).fechaEnvio(entity.getFechaEnvio())
                .fechaEstimadaEntrega(entity.getFechaEstimadaEntrega()).estado(entity.getEstado())
                .observaciones(entity.getObservaciones()).build();
    }

    private Encomienda convertToEntity(EncomiendaDTO dto) {
        return Encomienda.builder()
                .id(dto.getId()).codigoRastreo(dto.getCodigoRastreo())
                .remitente(dto.getRemitente()).dniRemitente(dto.getDniRemitente())
                .destinatario(dto.getDestinatario()).dniDestinatario(dto.getDniDestinatario())
                .origen(dto.getOrigen()).destino(dto.getDestino())
                .descripcion(dto.getDescripcion()).peso(dto.getPeso())
                .precio(dto.getPrecio()).fechaEnvio(dto.getFechaEnvio())
                .fechaEstimadaEntrega(dto.getFechaEstimadaEntrega()).estado(dto.getEstado())
                .observaciones(dto.getObservaciones()).build();
    }
}