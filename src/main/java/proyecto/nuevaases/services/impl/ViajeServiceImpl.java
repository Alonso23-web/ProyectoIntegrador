package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.ViajeService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViajeServiceImpl implements ViajeService {

    private final ViajeRepository viajeRepository;
    private final ReservaRepository reservaRepository;

    @Override
    public List<Viaje> buscar(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros) {
        // Disponibilidad real por asientos se maneja en el frontend calculando ocupación.
        // Aquí devolvemos viajes existentes para esa ruta/fecha.
        return viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fecha)
                .stream()
                .filter(v -> v.getTotalAsientos() >= (cantidadPasajeros != null ? cantidadPasajeros : 1))
                .toList();
    }

    @Override
    public java.util.Optional<Viaje> obtenerPorId(Long id) {
        return viajeRepository.findById(id);
    }
}

