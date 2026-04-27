package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Vehiculo;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByEstado(String estado);
    List<Vehiculo> findByTipo(String tipo);
    List<Vehiculo> findByEstadoAndTipo(String estado, String tipo);
}

