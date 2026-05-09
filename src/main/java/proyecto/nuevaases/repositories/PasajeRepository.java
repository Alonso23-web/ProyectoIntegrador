package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Pasaje;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PasajeRepository extends JpaRepository<Pasaje, Long> {
    List<Pasaje> findByEstado(String estado);

    List<Pasaje> findByFechaViaje(LocalDate fechaViaje);

    List<Pasaje> findByFechaViajeAndHoraViaje(LocalDate fechaViaje, String horaViaje);

    long countByDni(String dni);

    List<Pasaje> findTop5ByDniOrderByFechaViajeDesc(String dni);

    List<Pasaje> findTop5ByOrderByFechaViajeDesc();
}
