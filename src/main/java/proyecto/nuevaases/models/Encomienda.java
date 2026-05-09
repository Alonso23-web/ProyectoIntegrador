package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "encomiendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encomienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigoRastreo;

    @Column(nullable = false)
    private String remitente;

    @Column(nullable = false)
    private String dniRemitente;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String dniDestinatario;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    private String descripcion;

    @Column(nullable = false)
    private double peso;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private LocalDate fechaEnvio;

    private LocalDate fechaEstimadaEntrega;

    @Column(nullable = false)
    private String estado; // REGISTRADO, EN_TRANSITO, EN_DESTINO, ENTREGADO

    private String observaciones;

    private String creadoPorEmail;
}
