package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.nuevaases.models.Encomienda;

import java.util.List;
import java.util.Optional;

public interface EncomiendaRepository extends JpaRepository<Encomienda, Long> {

        Optional<Encomienda> findByCodigoRastreo(String codigoRastreo);

        List<Encomienda> findByDniRemitenteOrDniDestinatario(String dniRemitente, String dniDestinatario);

        List<Encomienda> findByDniRemitenteOrDniDestinatarioAndEstado(
                        String dniRemitente, String dniDestinatario, String estado);

        long countByDniRemitenteOrDniDestinatario(String dniRemitente, String dniDestinatario);

        List<Encomienda> findTop5ByDniRemitenteOrDniDestinatarioOrderByFechaEnvioDesc(
                        String dniRemitente, String dniDestinatario);

        long countByCreadoPorEmail(String creadoPorEmail);

        List<Encomienda> findTop5ByCreadoPorEmailOrderByFechaEnvioDesc(String creadoPorEmail);

        List<Encomienda> findByCreadoPorEmail(String creadoPorEmail);

        List<Encomienda> findTop5ByOrderByFechaEnvioDesc();
}
