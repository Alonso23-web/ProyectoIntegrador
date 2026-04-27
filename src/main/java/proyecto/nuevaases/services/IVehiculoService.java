package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.VehiculoDTO;
import java.util.List;

public interface IVehiculoService {
    List<VehiculoDTO> listarTodos();
    VehiculoDTO buscarPorId(Long id);
    VehiculoDTO guardar(VehiculoDTO vehiculoDTO);
    void eliminar(Long id);
}