package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.SolicitudAlquilerDTO;

import java.util.List;
import java.util.Optional;

public interface ISolicitudAlquilerService {
    SolicitudAlquilerDTO guardar(SolicitudAlquilerDTO dto);
    List<SolicitudAlquilerDTO> listarTodos();
    Optional<SolicitudAlquilerDTO> buscarPorId(Long id);
    void cambiarEstado(Long id, String nuevoEstado);
}
