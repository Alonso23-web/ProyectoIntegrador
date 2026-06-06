package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String dni;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String rol; // CLIENTE, ADMINISTRADOR, CONDUCTOR

    private boolean activo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaRegistro;

    // ==================== CAMPOS DE CONDUCTOR ====================

    private String numeroLicencia;

    private Integer aniosExperiencia;

    private String tipoVehiculo; // Minivan, Bus, Camión, Automóvil

    @Column(nullable = false)
    @Builder.Default
    private String estadoPostulacion = "PENDIENTE"; // PENDIENTE, APROBADO, RECHAZADO

    private String documentoUrl;
}

