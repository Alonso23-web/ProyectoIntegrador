package proyecto.nuevaases.services;

import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.repositories.VehiculoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    public Optional<Vehiculo> obtenerPorId(Long id) {
        return vehiculoRepository.findById(id);
    }

    public void guardar(Vehiculo vehiculo) {
        vehiculoRepository.save(vehiculo);
    }

    public void eliminar(Long id) {
        vehiculoRepository.deleteById(id);
    }
}
