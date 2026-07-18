package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.models.enums.EstadoEncomienda;

import java.util.List;
import java.util.Optional;

public interface EncomiendaRepository extends JpaRepository<Encomienda, Long> {

        Optional<Encomienda> findByCodigoRastreo(String codigoRastreo);

        List<Encomienda> findByDniRemitenteOrDniDestinatario(String dniRemitente, String dniDestinatario);

        List<Encomienda> findByDniRemitenteOrDniDestinatarioAndEstado(
                        String dniRemitente, String dniDestinatario, EstadoEncomienda estado);

        long countByDniRemitenteOrDniDestinatario(String dniRemitente, String dniDestinatario);

        List<Encomienda> findTop5ByDniRemitenteOrDniDestinatarioOrderByFechaEnvioDesc(
                        String dniRemitente, String dniDestinatario);

        long countByCreadoPorEmail(String creadoPorEmail);

        List<Encomienda> findTop5ByCreadoPorEmailOrderByFechaEnvioDesc(String creadoPorEmail);

        List<Encomienda> findByCreadoPorEmail(String creadoPorEmail);

        List<Encomienda> findTop5ByOrderByFechaEnvioDesc();

        long countByEstadoNot(EstadoEncomienda estado);

        List<Encomienda> findByEstado(EstadoEncomienda estado);

        @Query("SELECT e.estado as estado, COUNT(e) as total FROM Encomienda e GROUP BY e.estado")
        List<Object[]> countGroupByEstado();

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE Encomienda e SET e.viajeAsignado = NULL WHERE e.viajeAsignado.id = :viajeId")
        int desvincularEncomiendas(@Param("viajeId") Long viajeId);

        @Query("SELECT COUNT(e) FROM Encomienda e WHERE e.viajeAsignado.conductorEmail = :conductorEmail AND e.estado = :estado")
        long countByViajeConductorEmailAndEstado(@Param("conductorEmail") String conductorEmail, @Param("estado") EstadoEncomienda estado);
}
