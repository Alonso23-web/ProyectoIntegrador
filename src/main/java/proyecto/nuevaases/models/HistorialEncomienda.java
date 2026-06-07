package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_encomiendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEncomienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encomienda_id", nullable = false)
    private Encomienda encomienda;

    private String estadoAnterior;

    @Column(nullable = false)
    private String estadoNuevo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCambio;

    private String cambiadoPorEmail;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
