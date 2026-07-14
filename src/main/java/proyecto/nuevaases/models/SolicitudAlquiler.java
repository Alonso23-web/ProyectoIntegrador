package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_alquiler")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreSolicitante;

    private String empresa;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String tipoVehiculo;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Column(nullable = false)
    private Integer cantidadPersonas;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    private Double precioReferencial;

    private Integer horasPorDia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "PENDIENTE"; // PENDIENTE, CONTACTADO, CONFIRMADO, CANCELADO
}
