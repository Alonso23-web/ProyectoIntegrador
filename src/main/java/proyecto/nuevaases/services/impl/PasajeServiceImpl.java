package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoPasaje;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.IPasajeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasajeServiceImpl implements IPasajeService {

    private final PasajeRepository pasajeRepository;
    private final ViajeRepository viajeRepository;

    // ========================================================================
    // Admin CRUD
    // ========================================================================

    @Override
    public List<PasajeDTO> listarTodos() {
        return pasajeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PasajeDTO buscarPorId(Long id) {
        Pasaje pasaje = pasajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pasaje no encontrado con ID: " + id));
        return convertToDTO(pasaje);
    }

    @Override
    public PasajeDTO guardar(PasajeDTO dto) {
        Pasaje pasaje = convertToEntity(dto);
        if (dto.getViajeId() != null) {
            Viaje viaje = viajeRepository.findById(dto.getViajeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado con ID: " + dto.getViajeId()));
            pasaje.setViaje(viaje);
            if (pasaje.getOrigen() == null) pasaje.setOrigen(viaje.getOrigen());
            if (pasaje.getDestino() == null) pasaje.setDestino(viaje.getDestino());
            if (pasaje.getFechaViaje() == null) pasaje.setFechaViaje(viaje.getFecha());
            if (pasaje.getHoraViaje() == null) pasaje.setHoraViaje(viaje.getHoraSalida());
        }
        return convertToDTO(pasajeRepository.save(pasaje));
    }

    @Override
    public void eliminar(Long id) {
        if (!pasajeRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar el pasaje con ID: " + id);
        }
        pasajeRepository.deleteById(id);
    }

    // ========================================================================
    // Métodos entity-based (migrados de ReservaService)
    // ========================================================================

    @Override
    public List<Integer> asientosOcupados(Viaje viaje) {
        List<EstadoPasaje> estadosOcupados = List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO);
        return pasajeRepository.findByViajeAndEstadoIn(viaje, estadosOcupados)
                .stream()
                .map(Pasaje::getAsiento)
                .toList();
    }

    @Override
    public List<Integer> asientosOcupados(Long viajeId) {
        return pasajeRepository.findAsientosByViajeId(viajeId, List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO));
    }

    @Override
    public Pasaje reservar(String usuarioEmail, Viaje viaje, int asiento,
                            String nombrePasajero, String dniPasajero) {
        double precioViaje = viaje.getPrecio();
        String codigo = "B" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Pasaje pasaje = Pasaje.builder()
                .usuarioEmail(usuarioEmail)
                .viaje(viaje)
                .asiento(asiento)
                .estado(EstadoPasaje.RESERVADO)
                .codigoBoleto(codigo)
                .precio(precioViaje)
                .nombrePasajero(nombrePasajero)
                .dni(dniPasajero)
                .origen(viaje.getOrigen())
                .destino(viaje.getDestino())
                .fechaViaje(viaje.getFecha())
                .horaViaje(viaje.getHoraSalida())
                .build();

        return pasajeRepository.save(pasaje);
    }

    @Override
    public List<Pasaje> reservarMultiples(
            String usuarioEmail,
            Viaje viaje,
            List<PasajeroReservaDTO> pasajeros
    ) {
        double precioViaje = viaje.getPrecio();

        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar pasajeros");
        }

        // Contar ocupados actuales (RESERVADO o PAGADO)
        long ocupados = pasajeRepository.countByViajeAndEstadoIn(viaje, List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO));
        int disponibles = viaje.getTotalAsientos() - (int) ocupados;

        if (pasajeros.size() > disponibles) {
            throw new IllegalArgumentException(
                "No hay suficientes cupos disponibles. Solo quedan " + disponibles + " cupo(s)."
            );
        }

        // Crear pasajes SIN asignar asiento (asiento = 0)
        return pasajeros.stream().map(p -> {
            String codigo = "B" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            Pasaje pasaje = Pasaje.builder()
                    .usuarioEmail(usuarioEmail)
                    .viaje(viaje)
                    .asiento(0)  // Sin asiento asignado — se sientan donde haya espacio
                    .estado(EstadoPasaje.RESERVADO)
                    .codigoBoleto(codigo)
                    .precio(precioViaje)
                    .nombrePasajero(p.nombrePasajero())
                    .dni(p.dniPasajero())
                    .origen(viaje.getOrigen())
                    .destino(viaje.getDestino())
                    .fechaViaje(viaje.getFecha())
                    .horaViaje(viaje.getHoraSalida())
                    .build();

            return pasajeRepository.save(pasaje);
        }).toList();
    }

    @Override
    public Optional<Pasaje> obtenerPorCodigoBoleto(String codigoBoleto) {
        return pasajeRepository.findByCodigoBoleto(codigoBoleto);
    }

    @Override
    public List<Pasaje> misPasajes(String usuarioEmail) {
        return pasajeRepository.findByUsuarioEmailAndEstadoIn(
                usuarioEmail,
                List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO, EstadoPasaje.FINALIZADO)
        );
    }

    // ========================================================================
    // Métodos DTO-based (migrados de IReservaService)
    // ========================================================================

    @Override
    public PasajeDTO reservarDTO(String usuarioEmail, Long viajeId, int asiento,
                                  String nombrePasajero, String dniPasajero) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        Pasaje pasaje = this.reservar(usuarioEmail, viaje, asiento, nombrePasajero, dniPasajero);
        return convertToDTO(pasaje);
    }

    @Override
    public Optional<PasajeDTO> obtenerPasajeDTOPorCodigo(String codigoBoleto) {
        return pasajeRepository.findByCodigoBoleto(codigoBoleto).map(this::convertToDTO);
    }

    @Override
    public List<PasajeDTO> obtenerPasajesDTO(String usuarioEmail) {
        return misPasajes(usuarioEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // Conversión
    // ========================================================================

    private PasajeDTO convertToDTO(Pasaje entity) {
        PasajeDTO.PasajeDTOBuilder builder = PasajeDTO.builder()
                .id(entity.getId())
                .usuarioEmail(entity.getUsuarioEmail())
                .nombrePasajero(entity.getNombrePasajero())
                .dni(entity.getDni())
                .asiento(entity.getAsiento())
                .precio(entity.getPrecio())
                .estado(entity.getEstado().name())
                .creadoPorEmail(entity.getCreadoPorEmail())
                .codigoBoleto(entity.getCodigoBoleto());

        if (entity.getViaje() != null) {
            Viaje v = entity.getViaje();
            builder.viajeId(v.getId())
                    .origen(v.getOrigen())
                    .destino(v.getDestino())
                    .fechaViaje(v.getFecha())
                    .horaViaje(v.getHoraSalida());
        } else {
            builder.origen(entity.getOrigen())
                    .destino(entity.getDestino())
                    .fechaViaje(entity.getFechaViaje())
                    .horaViaje(entity.getHoraViaje());
        }

        return builder.build();
    }

    private Pasaje convertToEntity(PasajeDTO dto) {
        Pasaje.PasajeBuilder builder = Pasaje.builder()
                .id(dto.getId())
                .usuarioEmail(dto.getUsuarioEmail())
                .nombrePasajero(dto.getNombrePasajero())
                .dni(dto.getDni())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .fechaViaje(dto.getFechaViaje())
                .horaViaje(dto.getHoraViaje())
                .asiento(dto.getAsiento() != null ? dto.getAsiento() : 0)
                .precio(dto.getPrecio() != null ? dto.getPrecio() : 0.0)
                .estado(dto.getEstado() != null ? EstadoPasaje.valueOf(dto.getEstado()) : EstadoPasaje.RESERVADO)
                .creadoPorEmail(dto.getCreadoPorEmail())
                .codigoBoleto(dto.getCodigoBoleto());

        return builder.build();
    }
}
