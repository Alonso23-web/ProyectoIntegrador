package proyecto.nuevaases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proyecto.nuevaases.model.Viaje;

import java.time.LocalDate;
import java.util.List;

public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    List<Viaje> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha);
}