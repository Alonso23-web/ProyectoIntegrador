package proyecto.nuevaases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proyecto.nuevaases.model.Reserva;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @Query("SELECT r.asiento FROM Reserva r WHERE r.viaje.id = :viajeId AND r.estado = 'RESERVADO'")
    List<Integer> findAsientosByViajeId(@Param("viajeId") Long viajeId);
    List<Reserva> findByDniPasajero(String dniPasajero);
    Optional<Reserva> findByCodigoBoleto(String codigoBoleto);
}