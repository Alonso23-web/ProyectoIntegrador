package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.EncomiendaDTO;
import java.util.List;

public interface IEncomiendaService {
    List<EncomiendaDTO> listarTodos();
    EncomiendaDTO buscarPorId(Long id);
    EncomiendaDTO guardar(EncomiendaDTO encomiendaDTO);
    void eliminar(Long id);
}