package proyecto.nuevaases.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Viaje viaje;

    private Integer asiento;
    private String nombrePasajero;
    private String dniPasajero;
    private String estado; // RESERVADO, PAGADO, CANCELADO, (para UI: Programado, En ruta, Finalizado)
    private String codigoBoleto;
}