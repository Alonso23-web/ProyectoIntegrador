package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoPasaje;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasajeRepository extends JpaRepository<Pasaje, Long> {
    List<Pasaje> findByEstado(EstadoPasaje estado);

    List<Pasaje> findByFechaViaje(LocalDate fechaViaje);

    List<Pasaje> findByFechaViajeAndHoraViaje(LocalDate fechaViaje, String horaViaje);

    long countByDni(String dni);

    long countByCreadoPorEmail(String creadoPorEmail);

    List<Pasaje> findTop5ByDniOrderByFechaViajeDesc(String dni);

    List<Pasaje> findTop5ByCreadoPorEmailOrderByFechaViajeDesc(String creadoPorEmail);

    List<Pasaje> findTop5ByOrderByFechaViajeDesc();

    // === Queries migradas de ReservaRepository ===

    @Query("SELECT p.asiento FROM Pasaje p WHERE p.viaje.id = :viajeId AND p.estado IN :estados")
    List<Integer> findAsientosByViajeId(@Param("viajeId") Long viajeId, @Param("estados") List<EstadoPasaje> estados);

    List<Pasaje> findByViajeAndEstadoIn(Viaje viaje, List<EstadoPasaje> estados);

    long countByViajeAndEstadoIn(Viaje viaje, List<EstadoPasaje> estados);

    List<Pasaje> findByUsuarioEmailAndEstadoIn(String usuarioEmail, List<EstadoPasaje> estados);

    Optional<Pasaje> findByCodigoBoleto(String codigoBoleto);

    List<Pasaje> findByUsuarioEmailOrderByIdDesc(String usuarioEmail);

    @Query("SELECT COUNT(p) FROM Pasaje p WHERE p.viaje.fecha = :fecha")
    long countByViajeFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT COALESCE(SUM(p.precio), 0) FROM Pasaje p WHERE p.viaje.fecha = :fecha")
    double sumPrecioByViajeFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT p FROM Pasaje p WHERE p.viaje.fecha = :fecha ORDER BY p.id DESC")
    List<Pasaje> findByViajeFechaOrderByIdDesc(@Param("fecha") LocalDate fecha);

    @Query("SELECT COALESCE(SUM(p.precio), 0) FROM Pasaje p")
    double sumAllPrecio();

    @Query("SELECT p.viaje.fecha as fecha, COUNT(p) as total, SUM(p.precio) as totalPrecio " +
           "FROM Pasaje p WHERE p.viaje IS NOT NULL GROUP BY p.viaje.fecha ORDER BY p.viaje.fecha DESC")
    List<Object[]> countAndSumByFecha();

    @Query("SELECT COUNT(DISTINCT p.viaje.fecha) FROM Pasaje p WHERE p.viaje IS NOT NULL")
    long countDistinctViajeFecha();
}
