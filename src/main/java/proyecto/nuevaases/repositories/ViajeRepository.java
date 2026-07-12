package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoViaje;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    List<Viaje> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha);

    List<Viaje> findByFecha(LocalDate fecha);

    List<Viaje> findByConductorEmailAndFechaOrderByHoraSalida(String conductorEmail, LocalDate fecha);

    List<Viaje> findByConductorEmail(String conductorEmail);

    long countByConductorEmail(String conductorEmail);

    long countByConductorEmailAndEstadoViaje(String conductorEmail, EstadoViaje estadoViaje);

    long countByEstadoViajeAndFecha(EstadoViaje estado, LocalDate fecha);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Pasaje p WHERE p.viaje.id = :viajeId")
    int eliminarReservasAsociadas(@Param("viajeId") Long viajeId);

}

