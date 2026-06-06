package proyecto.nuevaases.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoDTO {

    private Long id;

    @NotBlank(message = "La placa es obligatoria.")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "La placa solo puede contener letras, números y guiones.")
    private String placa;

    @NotBlank(message = "La marca es obligatoria.")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio.")
    private String modelo;

    @NotNull(message = "El año es obligatorio.")
    @Min(value = 2000, message = "El año debe ser entre 2000 y 2030.")
    @Max(value = 2030, message = "El año debe ser entre 2000 y 2030.")
    private Integer anio;

    @NotNull(message = "La capacidad es obligatoria.")
    @Min(value = 1, message = "La capacidad debe ser entre 1 y 50.")
    @Max(value = 50, message = "La capacidad debe ser entre 1 y 50.")
    private Integer capacidad;

    @NotBlank(message = "El tipo es obligatorio.")
    private String tipo;

    @NotBlank(message = "El estado es obligatorio.")
    private String estado;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0.")
    private Double precioPorDia;

    private String imagen;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres.")
    private String descripcion;
}