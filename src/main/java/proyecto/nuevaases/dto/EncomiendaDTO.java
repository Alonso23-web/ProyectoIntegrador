package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncomiendaDTO {
    private Long id;
    private String codigoRastreo;
    private String remitente;
    private String dniRemitente;
    private String destinatario;
    private String dniDestinatario;
    private String origen;
    private String destino;
    private String descripcion;
    private double peso;
    private double precio;
    private LocalDate fechaEnvio;
    private LocalDate fechaEstimadaEntrega;
    private String estado;
    private String observaciones;
}