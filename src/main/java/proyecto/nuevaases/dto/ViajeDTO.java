package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViajeDTO {
    private Long id;
    private String origen;
    private String destino;
    private LocalDate fecha;
    private String horaSalida;
    private String tipoBus;
    private int totalAsientos;
    private double precio;
    private String creadoPorEmail;
}
