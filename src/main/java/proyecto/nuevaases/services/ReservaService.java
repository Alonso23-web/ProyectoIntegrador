package proyecto.nuevaases.services;

import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;

import java.util.List;
import java.util.Optional;

public interface ReservaService {

    List<Integer> asientosOcupados(Viaje viaje);

    Reserva reservar(String usuarioEmail, Viaje viaje, int asiento, double precio);

    Optional<Reserva> obtenerPorCodigoBoleto(String codigoBoleto);

    List<Reserva> misReservas(String usuarioEmail);
}

