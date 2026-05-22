package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r.asiento FROM Reserva r WHERE r.viaje.id = :viajeId AND r.estado IN ('RESERVADO', 'PAGADO')")
    List<Integer> findAsientosByViajeId(@Param("viajeId") Long viajeId);

    List<Reserva> findByViajeAndEstadoIn(Viaje viaje, List<String> estados);

    List<Reserva> findByUsuarioEmailAndEstadoIn(String usuarioEmail, List<String> estados);

    Optional<Reserva> findByCodigoBoleto(String codigoBoleto);

    List<Reserva> findByUsuarioEmailOrderByIdDesc(String usuarioEmail);

    List<Reserva> findTop5ByOrderByIdDesc();

}
