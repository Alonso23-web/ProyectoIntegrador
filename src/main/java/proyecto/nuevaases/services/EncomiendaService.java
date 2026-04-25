package proyecto.nuevaases.services;

import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.repositories.EncomiendaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EncomiendaService {

    private final EncomiendaRepository encomiendaRepository;

    public EncomiendaService(EncomiendaRepository encomiendaRepository) {
        this.encomiendaRepository = encomiendaRepository;
    }

    public List<Encomienda> listarTodos() {
        return encomiendaRepository.findAll();
    }

    public Optional<Encomienda> obtenerPorId(Long id) {
        return encomiendaRepository.findById(id);
    }

    public void guardar(Encomienda encomienda) {
        encomiendaRepository.save(encomienda);
    }

    public void eliminar(Long id) {
        encomiendaRepository.deleteById(id);
    }

    public java.util.Optional<Encomienda> buscarPorCodigoRastreo(String codigoRastreo) {
        return encomiendaRepository.findByCodigoRastreo(codigoRastreo);
    }
}
