package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import proyecto.nuevaases.models.enums.EstadoPostulacion;
import proyecto.nuevaases.models.enums.Rol;

import java.time.LocalDate;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Builder.Default
    private boolean activo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaRegistro;

    // ==================== CAMPOS DE CONDUCTOR ====================

    private String numeroLicencia;

    private Integer aniosExperiencia;

    private String tipoVehiculo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPostulacion estadoPostulacion = EstadoPostulacion.PENDIENTE;

    private String documentoUrl;

    // ==================== NUEVOS CAMPOS ====================

    private String direccion;

    private LocalDate fechaNacimiento;
}
