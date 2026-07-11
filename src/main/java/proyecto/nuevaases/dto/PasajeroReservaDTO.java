package proyecto.nuevaases.dto;

/**
 * DTO para recibir los datos de cada pasajero en una reserva múltiple.
 * Ya no incluye asiento porque NO se seleccionan asientos.
 */
public record PasajeroReservaDTO(
        String nombrePasajero,
        String dniPasajero
) {}
