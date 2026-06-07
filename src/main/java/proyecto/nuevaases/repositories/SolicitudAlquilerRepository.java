package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.SolicitudAlquiler;

import java.util.List;

@Repository
public interface SolicitudAlquilerRepository extends JpaRepository<SolicitudAlquiler, Long> {
    List<SolicitudAlquiler> findByEstadoOrderByFechaSolicitudDesc(String estado);
    List<SolicitudAlquiler> findAllByOrderByFechaSolicitudDesc();
    List<SolicitudAlquiler> findByCorreoOrderByFechaSolicitudDesc(String correo);
}
