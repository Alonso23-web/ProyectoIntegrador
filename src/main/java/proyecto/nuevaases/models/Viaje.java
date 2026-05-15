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
    private String horaSalida; // 08:00, 12:00, 16:00, 20:00

    @Column(nullable = false)
    private String tipoBus; // BUS, MINIVAN, CAMION

    @Column(nullable = false)
    private int totalAsientos;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private String creadoPorEmail;
}

