package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.ReservaDTO;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.services.IReservaService;
import proyecto.nuevaases.services.IPagoService;
import proyecto.nuevaases.services.ReservaService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService, IReservaService {

    private final ReservaRepository reservaRepository;
    private final ViajeRepository viajeRepository;
    private final IPagoService pagoService;

    // ========================================================================
    // Implementación de ReservaService (entity-based)
    // ========================================================================

    @Override
    public List<Integer> asientosOcupados(Viaje viaje) {
        List<String> estadosOcupados = List.of("RESERVADO", "PAGADO");
        return reservaRepository.findByViajeAndEstadoIn(viaje, estadosOcupados)
                .stream()
                .map(Reserva::getAsiento)
                .toList();
    }

    @Override
    public Reserva reservar(String usuarioEmail, Viaje viaje, int asiento,
                             String nombrePasajero, String dniPasajero) {
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

        Reserva guardada = reservaRepository.save(reserva);

        // Registrar pago automático
        pagoService.registrarPago(
                usuarioEmail,
                precioViaje,
                "EFECTIVO",
                guardada.getId(),
                "Pago automático - Boleto " + codigo
        );

        return guardada;
    }

    @Override
    public List<Reserva> reservarMultiples(
            String usuarioEmail,
            Viaje viaje,
            List<PasajeroReservaDTO> pasajeros
    ) {
        double precioViaje = viaje.getPrecio();

        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar pasajeros");
        }

        var asientosSolicitados = pasajeros.stream().map(PasajeroReservaDTO::asiento).toList();

        if (asientosSolicitados.size() != new java.util.HashSet<>(asientosSolicitados).size()) {
            throw new IllegalArgumentException("Hay asientos duplicados en la solicitud");
        }

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

        return pasajeros.stream().map(p -> {
            String codigo = "B" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            Reserva reserva = Reserva.builder()
                    .usuarioEmail(usuarioEmail)
                    .viaje(viaje)
                    .asiento(p.asiento())
                    .estado("RESERVADO")
                    .codigoBoleto(codigo)
                    .precio(precioViaje)
                    .nombrePasajero(p.nombrePasajero())
                    .dniPasajero(p.dniPasajero())
                    .build();

            Reserva guardada = reservaRepository.save(reserva);

            // Registrar pago automático por cada reserva
            pagoService.registrarPago(
                    usuarioEmail,
                    precioViaje,
                    "EFECTIVO",
                    guardada.getId(),
                    "Pago automático - Boleto " + codigo
            );

            return guardada;
        }).toList();
    }

    @Override
    public Optional<Reserva> obtenerPorCodigoBoleto(String codigoBoleto) {
        return reservaRepository.findByCodigoBoleto(codigoBoleto);
    }

    @Override
    public Optional<ReservaDTO> obtenerReservaDTOPorCodigo(String codigoBoleto) {
        return reservaRepository.findByCodigoBoleto(codigoBoleto).map(this::convertToDTO);
    }

    @Override
    public List<Reserva> misReservas(String usuarioEmail) {
        return reservaRepository.findByUsuarioEmailAndEstadoIn(
                usuarioEmail,
                List.of("RESERVADO", "PAGADO", "FINALIZADO")
        );
    }

    // ========================================================================
    // Implementación de IReservaService (DTO-based)
    // ========================================================================

    @Override
    public List<Integer> asientosOcupados(ViajeDTO viajeDTO) {
        Viaje viaje = viajeRepository.findById(viajeDTO.getId())
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        return asientosOcupados(viaje);
    }

    @Override
    public ReservaDTO reservar(String usuarioEmail, ViajeDTO viajeDTO, int asiento,
                                String nombrePasajero, String dniPasajero) {
        Viaje viaje = viajeRepository.findById(viajeDTO.getId())
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        Reserva reserva = this.reservar(usuarioEmail, viaje, asiento, nombrePasajero, dniPasajero);
        return convertToDTO(reserva);
    }

    @Override
    public List<ReservaDTO> obtenerReservasDTO(String usuarioEmail) {
        return misReservas(usuarioEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Reserva a su DTO con todos los campos de ViajeDTO.
     */
    private ReservaDTO convertToDTO(Reserva entity) {
        Viaje v = entity.getViaje();
        ViajeDTO.ViajeDTOBuilder viajeBuilder = ViajeDTO.builder()
                .id(v.getId())
                .origen(v.getOrigen())
                .destino(v.getDestino())
                .fecha(v.getFecha())
                .horaSalida(v.getHoraSalida())
                .tipoBus(v.getTipoBus())
                .totalAsientos(v.getTotalAsientos())
                .precio(v.getPrecio())
                .creadoPorEmail(v.getCreadoPorEmail())
                .conductorEmail(v.getConductorEmail())
                .estadoViaje(v.getEstadoViaje());

        if (v.getVehiculo() != null) {
            Vehiculo veh = v.getVehiculo();
            viajeBuilder.vehiculoId(veh.getId())
                    .vehiculoInfo(veh.getMarca() + " " + veh.getModelo() + " - " + veh.getPlaca());
        }

        return ReservaDTO.builder()
                .id(entity.getId())
                .usuarioEmail(entity.getUsuarioEmail())
                .viaje(viajeBuilder.build())
                .nombrePasajero(entity.getNombrePasajero())
                .dniPasajero(entity.getDniPasajero())
                .asiento(entity.getAsiento())
                .estado(entity.getEstado())
                .codigoBoleto(entity.getCodigoBoleto())
                .precio(entity.getPrecio())
                .build();
    }
}
