package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.HistorialEncomienda;

import java.util.List;

@Repository
public interface HistorialEncomiendaRepository extends JpaRepository<HistorialEncomienda, Long> {
    List<HistorialEncomienda> findByEncomiendaIdOrderByFechaCambioDesc(Long encomiendaId);
}
