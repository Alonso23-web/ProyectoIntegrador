package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.dto.PagoDTO;
import proyecto.nuevaases.models.Pago;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.repositories.PagoRepository;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.services.IPagoService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements IPagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    @Override
    public PagoDTO registrarPago(String usuarioEmail, Double monto, String metodoPago,
                                  Long reservaId, String referencia) {
        Reserva reserva = null;
        if (reservaId != null) {
            reserva = reservaRepository.findById(reservaId).orElse(null);
        }

        Pago pago = Pago.builder()
                .usuarioEmail(usuarioEmail)
                .monto(monto)
                .metodoPago(metodoPago != null ? metodoPago : "EFECTIVO")
                .estado("PAGADO")
                .referencia(referencia)
                .reserva(reserva)
                .build();

        return convertToDTO(pagoRepository.save(pago));
    }

    @Override
    public List<PagoDTO> listarPorUsuario(String usuarioEmail) {
        return pagoRepository.findByUsuarioEmailOrderByFechaPagoDesc(usuarioEmail)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PagoDTO> listarPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PagoDTO> listarTodos() {
        return pagoRepository.findAll().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    private PagoDTO convertToDTO(Pago entity) {
        PagoDTO.PagoDTOBuilder builder = PagoDTO.builder()
                .id(entity.getId())
                .usuarioEmail(entity.getUsuarioEmail())
                .monto(entity.getMonto())
                .fechaPago(entity.getFechaPago())
                .metodoPago(entity.getMetodoPago())
                .estado(entity.getEstado())
                .referencia(entity.getReferencia());

        if (entity.getReserva() != null) {
            builder.reservaId(entity.getReserva().getId());
            builder.codigoBoleto(entity.getReserva().getCodigoBoleto());
        }

        return builder.build();
    }
}
