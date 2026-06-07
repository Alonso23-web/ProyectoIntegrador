package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEncomiendaDTO {
    private Long id;
    private Long encomiendaId;
    private String codigoRastreo;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private String cambiadoPorEmail;
    private String observaciones;
}
