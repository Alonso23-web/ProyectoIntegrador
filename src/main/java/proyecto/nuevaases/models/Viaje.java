package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    @Builder.Default
    private String estadoViaje = "PROGRAMADO";

    // ==================== VEHÍCULO ASIGNADO ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;
}

