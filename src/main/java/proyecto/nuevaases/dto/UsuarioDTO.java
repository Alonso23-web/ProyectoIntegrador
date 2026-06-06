package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String email;
    private String nombreCompleto;
    private String dni;
    private String telefono;
    private String rol;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    // Campos de conductor
    private String numeroLicencia;
    private Integer aniosExperiencia;
    private String tipoVehiculo;
    private String estadoPostulacion;
    private String documentoUrl;
}
