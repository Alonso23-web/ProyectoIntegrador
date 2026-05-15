package proyecto.nuevaases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.nuevaases.model.Encomienda;

import java.util.List;
import java.util.Optional;

public interface EncomiendaRepository extends JpaRepository<Encomienda, Long> {
    Optional<Encomienda> findByCodigoRastreo(String codigoRastreo);
    // En una aplicación real, esto se filtraría por el usuario autenticado.
    List<Encomienda> findByDniRemitente(String dniRemitente);
}