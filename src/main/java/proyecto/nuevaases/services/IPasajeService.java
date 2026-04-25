package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.PasajeDTO;
import java.util.List;

public interface IPasajeService {
    List<PasajeDTO> listarTodos();
    PasajeDTO buscarPorId(Long id);
    PasajeDTO guardar(PasajeDTO pasajeDTO);
    void eliminar(Long id);
}