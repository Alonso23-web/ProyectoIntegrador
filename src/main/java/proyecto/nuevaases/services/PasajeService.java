package proyecto.nuevaases.services;

import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.repositories.PasajeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PasajeService {

    private final PasajeRepository pasajeRepository;

    public PasajeService(PasajeRepository pasajeRepository) {
        this.pasajeRepository = pasajeRepository;
    }

    public List<Pasaje> listarTodos() {
        return pasajeRepository.findAll();
    }

    public Optional<Pasaje> obtenerPorId(Long id) {
        return pasajeRepository.findById(id);
    }

    public void guardar(Pasaje pasaje) {
        pasajeRepository.save(pasaje);
    }

    public void eliminar(Long id) {
        pasajeRepository.deleteById(id);
    }
}
