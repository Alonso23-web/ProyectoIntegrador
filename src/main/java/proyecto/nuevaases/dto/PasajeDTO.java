package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasajeDTO {
    private Long id;
    private String nombrePasajero;
    private String dni;
    private String origen;
    private String destino;
    private LocalDate fechaViaje;
    private String horaViaje;
    private Integer asiento;
    private Double precio;
    private String estado;
}
