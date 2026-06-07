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

    // ==================== CAMPOS AGREGADOS ====================
    private String conductorEmail;
    private String estadoViaje;
    private Long vehiculoId;
    private String vehiculoInfo; // Marca + Modelo + Placa
}
