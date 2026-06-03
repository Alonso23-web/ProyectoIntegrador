package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.ReservaDTO;
import proyecto.nuevaases.dto.ViajeDTO;

import java.util.List;
import java.util.Optional;

public interface IReservaService {
    List<Integer> asientosOcupados(ViajeDTO viajeDTO);
    ReservaDTO reservar(String usuarioEmail, ViajeDTO viajeDTO, int asiento, String nombrePasajero, String dniPasajero);
    Optional<ReservaDTO> obtenerReservaDTOPorCodigo(String codigoBoleto);
    List<ReservaDTO> obtenerReservasDTO(String usuarioEmail);
}
