package proyecto.nuevaases.models;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "tipo", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'BUS'")
    private String tipo = "BUS"; // BUS, MINIVAN, CAMION

    // Columna legacy: indica si el vehículo es PROPIO de la empresa, ALQUILADO o de TERCEROS
    // Siempre será "PROPIO" para este negocio, nunca se expone en el formulario
    @Builder.Default
    @Column(name = "tipo_propiedad", nullable = false, updatable = false)
    private String tipoPropiedad = "PROPIO";

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'DISPONIBLE'")
    private String estado; // DISPONIBLE, ALQUILADO, MANTENIMIENTO

    @Column(nullable = false)
    private double precioPorDia;

    private String imagen; // URL de la imagen

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}

