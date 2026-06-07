package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.SolicitudAlquilerDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.SolicitudAlquiler;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.repositories.SolicitudAlquilerRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.ISolicitudAlquilerService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudAlquilerServiceImpl implements ISolicitudAlquilerService {

    private final SolicitudAlquilerRepository solicitudAlquilerRepository;
    private final VehiculoRepository vehiculoRepository;

    @Override
    public SolicitudAlquilerDTO guardar(SolicitudAlquilerDTO dto) {
        SolicitudAlquiler entity = convertToEntity(dto);
        return convertToDTO(solicitudAlquilerRepository.save(entity));
    }

    @Override
    public List<SolicitudAlquilerDTO> listarTodos() {
        return solicitudAlquilerRepository.findAllByOrderByFechaSolicitudDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SolicitudAlquilerDTO> buscarPorId(Long id) {
        return solicitudAlquilerRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public void cambiarEstado(Long id, String nuevoEstado) {
        SolicitudAlquiler entity = solicitudAlquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));
        entity.setEstado(nuevoEstado);
        solicitudAlquilerRepository.save(entity);
    }

    private SolicitudAlquilerDTO convertToDTO(SolicitudAlquiler entity) {
        SolicitudAlquilerDTO.SolicitudAlquilerDTOBuilder builder = SolicitudAlquilerDTO.builder()
                .id(entity.getId())
                .nombreSolicitante(entity.getNombreSolicitante())
                .empresa(entity.getEmpresa())
                .telefono(entity.getTelefono())
                .correo(entity.getCorreo())
                .tipoVehiculo(entity.getTipoVehiculo())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .cantidadPersonas(entity.getCantidadPersonas())
                .origen(entity.getOrigen())
                .destino(entity.getDestino())
                .mensaje(entity.getMensaje())
                .fechaSolicitud(entity.getFechaSolicitud())
                .estado(entity.getEstado());

        if (entity.getVehiculo() != null) {
            Vehiculo v = entity.getVehiculo();
            builder.vehiculoId(v.getId());
            builder.vehiculoInfo(v.getMarca() + " " + v.getModelo() + " - " + v.getPlaca());
        }

        return builder.build();
    }

    private SolicitudAlquiler convertToEntity(SolicitudAlquilerDTO dto) {
        SolicitudAlquiler.SolicitudAlquilerBuilder builder = SolicitudAlquiler.builder()
                .id(dto.getId())
                .nombreSolicitante(dto.getNombreSolicitante())
                .empresa(dto.getEmpresa())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .tipoVehiculo(dto.getTipoVehiculo())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .cantidadPersonas(dto.getCantidadPersonas())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .mensaje(dto.getMensaje())
                .estado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");

        if (dto.getVehiculoId() != null) {
            vehiculoRepository.findById(dto.getVehiculoId()).ifPresent(builder::vehiculo);
        }

        return builder.build();
    }
}
