package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.models.Viaje;

import java.util.List;
import java.util.Optional;

public interface IPasajeService {
    // Admin CRUD
    List<PasajeDTO> listarTodos();
    PasajeDTO buscarPorId(Long id);
    PasajeDTO guardar(PasajeDTO pasajeDTO);
    void eliminar(Long id);

    // === Métodos migrados de ReservaService (entity-based) ===
    List<Integer> asientosOcupados(Viaje viaje);
    List<Integer> asientosOcupados(Long viajeId);
    Pasaje reservar(String usuarioEmail, Viaje viaje, int asiento, String nombrePasajero, String dniPasajero);
    List<Pasaje> reservarMultiples(String usuarioEmail, Viaje viaje, List<PasajeroReservaDTO> pasajeros);
    Optional<Pasaje> obtenerPorCodigoBoleto(String codigoBoleto);
    List<Pasaje> misPasajes(String usuarioEmail);

    // === Métodos migrados de IReservaService (DTO-based) ===
    PasajeDTO reservarDTO(String usuarioEmail, Long viajeId, int asiento, String nombrePasajero, String dniPasajero);
    Optional<PasajeDTO> obtenerPasajeDTOPorCodigo(String codigoBoleto);
    List<PasajeDTO> obtenerPasajesDTO(String usuarioEmail);
}
