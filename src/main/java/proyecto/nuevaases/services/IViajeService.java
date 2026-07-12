package proyecto.nuevaases.services;

import proyecto.nuevaases.dto.ViajeDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IViajeService {
    List<ViajeDTO> listarTodosDTO();
    Optional<ViajeDTO> obtenerPorIdDTO(Long id);
    ViajeDTO guardarDTO(ViajeDTO viajeDTO);
    void eliminarDTO(Long id);
    List<ViajeDTO> buscarDTO(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros);

    /**
     * Genera múltiples viajes en lote según los parámetros indicados.
     *
     * @return número total de viajes creados
     */
    int generarMasivo(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String origen,
            String destino,
            boolean generarInverso,
            List<String> horarios,
            String tipoBus,
            int totalAsientos,
            double precio,
            String creadoPorEmail,
            String conductorEmail,
            Long vehiculoId,
            String estadoViaje
    );
}
