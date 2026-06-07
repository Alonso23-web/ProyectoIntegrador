package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.PagoDTO;

import java.util.List;
import java.util.Optional;

public interface IPagoService {
    PagoDTO registrarPago(String usuarioEmail, Double monto, String metodoPago,
                           Long reservaId, String referencia);
    List<PagoDTO> listarPorUsuario(String usuarioEmail);
    List<PagoDTO> listarPorReserva(Long reservaId);
    List<PagoDTO> listarTodos();
}
