package proyecto.nuevaases.dto;

/**
 * DTO para recibir los datos de cada pasajero en una reserva múltiple.
 */
public record PasajeroReservaDTO(
        String nombrePasajero,
        String dniPasajero,
        int asiento
) {}
