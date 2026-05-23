package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.ViajeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ViajeServiceImpl implements ViajeService {

    private final ViajeRepository viajeRepository;
    private final ReservaRepository reservaRepository;

    @Override
    public List<Viaje> buscar(String origen, String destino, LocalDate fecha, Integer cantidadPasajeros) {
        return viajeRepository.findByOrigenAndDestinoAndFecha(origen, destino, fecha)
                .stream()
                .filter(v -> v.getTotalAsientos() >= (cantidadPasajeros != null ? cantidadPasajeros : 1))
                .toList();
    }

    @Override
    public Optional<Viaje> obtenerPorId(Long id) {
        return viajeRepository.findById(id);
    }

    @Override
    public List<Viaje> listarTodos() {
        return viajeRepository.findAll();
    }

    @Override
    public void guardar(Viaje viaje) {
        viajeRepository.save(viaje);
    }

    @Override
    public void eliminar(Long id) {
        viajeRepository.deleteById(id);
    }
}
