package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuarioEmail;

    @Column(nullable = false)
    private Double monto;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false)
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA

    @Column(nullable = false)
    @Builder.Default
    private String estado = "PAGADO"; // PENDIENTE, PAGADO, REEMBOLSADO

    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
}
