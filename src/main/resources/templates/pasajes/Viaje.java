package proyecto.nuevaases.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class Viaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String origen;
    private String destino;
    private LocalDate fecha;
    private LocalTime horaSalida; // Renamed from hora
    private Double precio;
    private String tipoBus; // Ej: Ejecutivo, Semicama
    private String horaLlegada;
    private Integer totalAsientos = 30; // Renamed from capacidadTotal
}