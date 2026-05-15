package proyecto.nuevaases.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Encomienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String remitente;
    private String dniRemitente;
    private String destinatario;
    private String dniDestinatario;
    private String origen;
    private String destino;
    private Double pesoEstimado;
    private Double costoEstimado;
    private String tipoPaquete; // Usado para la descripción en el formulario
    private String observaciones; // Usado para el teléfono de contacto en el formulario
    private String estado; // REGISTRADO, VERIFICADO, EN_TRANSITO, ENTREGADO
    private String codigoRastreo;
    private LocalDate fechaEnvio;
    private LocalDate fechaEstimadaEntrega;
    private Double pesoReal;
    private Double costoFinal;
}