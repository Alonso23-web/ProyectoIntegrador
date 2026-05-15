package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuarioEmail;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(nullable = false)
    private int asiento;

    @Column(nullable = false)
    private String estado; // RESERVADO, PAGADO, CANCELADO, FINALIZADO

    @Column(nullable = false, unique = true)
    private String codigoBoleto;

    @Column(nullable = false)
    private double precio;
}

