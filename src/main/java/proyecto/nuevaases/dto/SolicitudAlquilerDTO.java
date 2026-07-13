package proyecto.nuevaases.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAlquilerDTO {
    private Long id;
    private String nombreSolicitante;
    private String empresa;
    private String telefono;
    private String correo;
    private String tipoVehiculo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cantidadPersonas;
    private String origen;
    private String destino;
    private String mensaje;
    private Long vehiculoId;
    private String vehiculoInfo; // Marca + Modelo + Placa para mostrar en vista
    private LocalDateTime fechaSolicitud;
    private String estado;
    private Double precioReferencial;
    private Integer horasPorDia;
}
