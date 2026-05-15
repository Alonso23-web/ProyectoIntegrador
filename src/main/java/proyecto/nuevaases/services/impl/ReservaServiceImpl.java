package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.services.ReservaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;

    @Override
    public List<Integer> asientosOcupados(Viaje viaje) {
        List<String> estadosOcupados = List.of("RESERVADO", "PAGADO");
        return reservaRepository.findByViajeAndEstadoIn(viaje, estadosOcupados)
                .stream()
                .map(Reserva::getAsiento)
                .toList();
    }

    @Override
    public Reserva reservar(String usuarioEmail, Viaje viaje, int asiento, double precio) {
        // Generación de código boleto
        String codigo = "B" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Reserva reserva = Reserva.builder()
                .usuarioEmail(usuarioEmail)
                .viaje(viaje)
                .asiento(asiento)
                .estado("RESERVADO")
                .codigoBoleto(codigo)
                .precio(precio)
                .build();

        return reservaRepository.save(reserva);
    }

    @Override
    public Optional<Reserva> obtenerPorCodigoBoleto(String codigoBoleto) {
        return reservaRepository.findByCodigoBoleto(codigoBoleto);
    }

    @Override
    public List<Reserva> misReservas(String usuarioEmail) {
        return reservaRepository.findByUsuarioEmailAndEstadoIn(
                usuarioEmail,
                List.of("RESERVADO", "PAGADO", "FINALIZADO")
        );

    }
}

