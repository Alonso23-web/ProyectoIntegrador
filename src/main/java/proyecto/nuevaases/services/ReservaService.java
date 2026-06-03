package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;

import java.util.List;
import java.util.Optional;

public interface ReservaService {

    List<Integer> asientosOcupados(Viaje viaje);

    Reserva reservar(String usuarioEmail, Viaje viaje, int asiento, String nombrePasajero, String dniPasajero);

    List<Reserva> reservarMultiples(
            String usuarioEmail,
            Viaje viaje,
            List<PasajeroReservaDTO> pasajeros
    );

    Optional<Reserva> obtenerPorCodigoBoleto(String codigoBoleto);

    List<Reserva> misReservas(String usuarioEmail);
}

