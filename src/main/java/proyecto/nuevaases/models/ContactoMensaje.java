package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacto_mensajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String correo;

    private String telefono;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private boolean leido = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
