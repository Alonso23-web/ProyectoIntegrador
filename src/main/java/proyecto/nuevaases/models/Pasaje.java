package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import proyecto.nuevaases.models.enums.EstadoPasaje;
import proyecto.nuevaases.models.enums.EstadoPasajeViaje;
import proyecto.nuevaases.models.enums.TipoPago;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private String usuarioEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id")
    private Viaje viaje;

    @Column(nullable = false)
    private String nombrePasajero;

    @Column(nullable = false)
    private String dni;

    @Builder.Default
    @Column(nullable = false)
    private String origen = "Trujillo";

    @Builder.Default
    @Column(nullable = false)
    private String destino = "Chepén";

    @Column(nullable = false)
    private LocalDate fechaViaje;

    @Column(nullable = false)
    private String horaViaje;

    @Column(nullable = false)
    private int asiento;

    @Column(nullable = false)
    private double precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPasaje estado;

    private String creadoPorEmail;

    @Column(unique = true)
    private String codigoBoleto;

    // ==================== NUEVOS CAMPOS ====================

    private String telefonoPasajero;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPasajeViaje estadoViaje;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCompra;
}
