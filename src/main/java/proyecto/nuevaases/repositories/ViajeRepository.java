package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Viaje;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    List<Viaje> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha);

    List<Viaje> findByFecha(LocalDate fecha);

    List<Viaje> findByConductorEmailAndFechaOrderByHoraSalida(String conductorEmail, LocalDate fecha);

    List<Viaje> findByConductorEmail(String conductorEmail);

    long countByConductorEmail(String conductorEmail);

    long countByConductorEmailAndEstadoViaje(String conductorEmail, String estadoViaje);

}

