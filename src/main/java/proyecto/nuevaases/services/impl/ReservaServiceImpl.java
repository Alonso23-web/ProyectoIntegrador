package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.services.ReservaService;
import proyecto.nuevaases.controllers.api.PasajesApiController;





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
    public Reserva reservar(String usuarioEmail, Viaje viaje, int asiento, String nombrePasajero, String dniPasajero) {
        double precioViaje = viaje.getPrecio();
        String codigo = "B" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Reserva reserva = Reserva.builder()
                .usuarioEmail(usuarioEmail)
                .viaje(viaje)
                .asiento(asiento)
                .estado("RESERVADO")
                .codigoBoleto(codigo)
                .precio(precioViaje)
                .nombrePasajero(nombrePasajero)
                .dniPasajero(dniPasajero)
                .build();

        return reservaRepository.save(reserva);
    }

    @Override
    public List<Reserva> reservarMultiples(
            String usuarioEmail,
            Viaje viaje,
            List<PasajesApiController.PasajeroReservaDTO> pasajeros
    ) {

        double precioViaje = viaje.getPrecio();

        // Validación: cantidad no nula
        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar pasajeros");
        }

        // Validar duplicados de asiento en el request
        var asientosSolicitados = pasajeros.stream().map(PasajesApiController.PasajeroReservaDTO::asiento).toList();

        if (asientosSolicitados.size() != new java.util.HashSet<>(asientosSolicitados).size()) {
            throw new IllegalArgumentException("Hay asientos duplicados en la solicitud");
        }

        // Validar disponibilidad (ocupados ya reservados/pagados)
        List<Integer> ocupados = reservaRepository.findByViajeAndEstadoIn(viaje, List.of("RESERVADO", "PAGADO"))
                .stream()
                .map(Reserva::getAsiento)
                .toList();

        var ocupadosSet = new java.util.HashSet<>(ocupados);
        for (var p : pasajeros) {

            if (ocupadosSet.contains(p.asiento())) {
                throw new IllegalArgumentException("El asiento " + p.asiento() + " ya se encuentra ocupado");
            }
            if (p.asiento() < 1 || p.asiento() > viaje.getTotalAsientos()) {
                throw new IllegalArgumentException("El asiento " + p.asiento() + " no es válido para este viaje");
            }
        }

        // Crear N reservas
        return pasajeros.stream().map(p -> {
            String codigo = "B" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            return Reserva.builder()
                    .usuarioEmail(usuarioEmail)
                    .viaje(viaje)
                    .asiento(p.asiento())
                    .estado("RESERVADO")
                    .codigoBoleto(codigo)
                    .precio(precioViaje)
                    .nombrePasajero(p.nombrePasajero())
                    .dniPasajero(p.dniPasajero())
                    .build();
        }).map(reservaRepository::save).toList();
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

