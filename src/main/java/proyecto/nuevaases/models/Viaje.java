package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import proyecto.nuevaases.models.enums.EstadoViaje;

import java.time.LocalDate;

@Entity
@Table(name = "viajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String horaSalida;

    @Column(nullable = false)
    private String tipoBus;

    @Column(nullable = false)
    private int totalAsientos;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private String creadoPorEmail;

    // ==================== CAMPOS DE ASIGNACIÓN ====================

    private String conductorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoViaje estadoViaje = EstadoViaje.PROGRAMADO;

    // ==================== VEHÍCULO ASIGNADO ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    // ==================== NUEVOS CAMPOS ====================

    private String horaLlegadaEstimada;

    private Integer duracionEstimada;
}
