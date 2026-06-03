package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {
    private Long id;
    private String usuarioEmail;
    private ViajeDTO viaje;
    private String nombrePasajero;
    private String dniPasajero;
    private int asiento;
    private String estado;
    private String codigoBoleto;
    private double precio;
}
