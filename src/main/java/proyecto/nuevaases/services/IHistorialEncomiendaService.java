package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.HistorialEncomiendaDTO;

import java.util.List;

public interface IHistorialEncomiendaService {
    List<HistorialEncomiendaDTO> obtenerHistorial(Long encomiendaId);
    void registrarCambio(Long encomiendaId, String estadoAnterior, String estadoNuevo,
                          String cambiadoPorEmail, String observaciones);
}
