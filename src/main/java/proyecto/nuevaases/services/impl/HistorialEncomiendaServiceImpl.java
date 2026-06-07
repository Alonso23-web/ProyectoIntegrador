package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.HistorialEncomiendaDTO;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.models.HistorialEncomienda;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.repositories.HistorialEncomiendaRepository;
import proyecto.nuevaases.services.IHistorialEncomiendaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialEncomiendaServiceImpl implements IHistorialEncomiendaService {

    private final HistorialEncomiendaRepository historialEncomiendaRepository;
    private final EncomiendaRepository encomiendaRepository;

    @Override
    public List<HistorialEncomiendaDTO> obtenerHistorial(Long encomiendaId) {
        return historialEncomiendaRepository.findByEncomiendaIdOrderByFechaCambioDesc(encomiendaId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void registrarCambio(Long encomiendaId, String estadoAnterior, String estadoNuevo,
                                 String cambiadoPorEmail, String observaciones) {
        Encomienda encomienda = encomiendaRepository.findById(encomiendaId)
                .orElseThrow(() -> new RuntimeException("Encomienda no encontrada con ID: " + encomiendaId));

        HistorialEncomienda historial = HistorialEncomienda.builder()
                .encomienda(encomienda)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .cambiadoPorEmail(cambiadoPorEmail)
                .observaciones(observaciones)
                .build();

        historialEncomiendaRepository.save(historial);
    }

    private HistorialEncomiendaDTO convertToDTO(HistorialEncomienda entity) {
        return HistorialEncomiendaDTO.builder()
                .id(entity.getId())
                .encomiendaId(entity.getEncomienda().getId())
                .codigoRastreo(entity.getEncomienda().getCodigoRastreo())
                .estadoAnterior(entity.getEstadoAnterior())
                .estadoNuevo(entity.getEstadoNuevo())
                .fechaCambio(entity.getFechaCambio())
                .cambiadoPorEmail(entity.getCambiadoPorEmail())
                .observaciones(entity.getObservaciones())
                .build();
    }
}
