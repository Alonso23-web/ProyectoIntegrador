package proyecto.nuevaases.services;

import proyecto.nuevaases.models.Viaje;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ViajeService {

    List<Viaje> buscar(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros);

    Optional<Viaje> obtenerPorId(Long id);

}

