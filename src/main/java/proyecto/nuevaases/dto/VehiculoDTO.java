package proyecto.nuevaases.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoDTO {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private int capacidad;
    private String tipo;
    private String estado;
    private double precioPorDia;
    private String imagen;
}