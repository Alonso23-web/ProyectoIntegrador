package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.ViajeDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IViajeService {
    List<ViajeDTO> listarTodosDTO();
    Optional<ViajeDTO> obtenerPorIdDTO(Long id);
    ViajeDTO guardarDTO(ViajeDTO viajeDTO);
    void eliminarDTO(Long id);
    List<ViajeDTO> buscarDTO(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros);
}
