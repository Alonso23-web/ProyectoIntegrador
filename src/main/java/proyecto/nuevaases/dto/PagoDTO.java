package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {
    private Long id;
    private String usuarioEmail;
    private Double monto;
    private LocalDateTime fechaPago;
    private String metodoPago;
    private String estado;
    private String referencia;
    private Long reservaId;
    private String codigoBoleto;
}
