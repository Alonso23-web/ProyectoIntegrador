package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Encomienda;

import java.util.Optional;

@Repository
public interface EncomiendaRepository extends JpaRepository<Encomienda, Long> {
    Optional<Encomienda> findByCodigoRastreo(String codigoRastreo);
}

