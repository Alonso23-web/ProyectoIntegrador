package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByViajeAndEstadoIn(Viaje viaje, List<String> estados);

    List<Reserva> findByUsuarioEmailAndEstadoIn(String usuarioEmail, List<String> estados);

    Optional<Reserva> findByCodigoBoleto(String codigoBoleto);

    // Nota: se deja fuera por mapeo compuesto; se puede refinar después.
    List<Reserva> findByUsuarioEmailOrderByIdDesc(String usuarioEmail);

}

