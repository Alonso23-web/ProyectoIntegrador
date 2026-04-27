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

    @Column(nullable = false)
    private String tipo; // BUS, MINIVAN, CAMION

    @Column(nullable = false)
    private String estado; // DISPONIBLE, ALQUILADO, MANTENIMIENTO

    @Column(nullable = false)
    private double precioPorDia;

    private String imagen; // URL de la imagen
}

