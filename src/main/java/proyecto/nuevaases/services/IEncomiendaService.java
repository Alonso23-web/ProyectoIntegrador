package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.EncomiendaDTO;
import java.util.List;
import java.util.Optional;

public interface IEncomiendaService {
    List<EncomiendaDTO> listarTodos();
    EncomiendaDTO buscarPorId(Long id);
    EncomiendaDTO guardar(EncomiendaDTO encomiendaDTO);
    void eliminar(Long id);
    Optional<EncomiendaDTO> buscarPorCodigoRastreo(String codigoRastreo);
    List<EncomiendaDTO> buscarPorDni(String dni, String estado);
    List<EncomiendaDTO> buscarPorCreadoPorEmail(String creadoPorEmail);
    double calcularPrecio(String origen, String destino, double peso);
}