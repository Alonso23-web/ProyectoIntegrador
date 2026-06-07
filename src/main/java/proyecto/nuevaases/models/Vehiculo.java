package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;
import proyecto.nuevaases.models.enums.EstadoVehiculo;
import proyecto.nuevaases.models.enums.TipoVehiculo;

import java.time.LocalDate;

@Entity
@Table(name = "vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String placa;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoVehiculo tipo = TipoVehiculo.BUS;

    @Builder.Default
    @Column(name = "tipo_propiedad", nullable = false, updatable = false)
    private String tipoPropiedad = "PROPIO";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Column(nullable = false)
    private double precioPorDia;

    private String imagen;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // ==================== NUEVOS CAMPOS ====================

    private LocalDate fechaUltimoMantenimiento;

    private LocalDate fechaProximoMantenimiento;

    private Boolean aireAcondicionado;

    private Boolean wifi;
}
