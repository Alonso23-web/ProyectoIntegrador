package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "pasajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pasaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombrePasajero;

    @Column(nullable = false)
    private String dni;

    @Column(nullable = false)
    private String origen = "Trujillo";

    @Column(nullable = false)
    private String destino = "Chepén";

    @Column(nullable = false)
    private LocalDate fechaViaje;

    @Column(nullable = false)
    private String horaViaje; // 08:00, 12:00, 16:00, 20:00

    @Column(nullable = false)
    private int asiento;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private String estado; // RESERVADO, PAGADO, CANCELADO
}

