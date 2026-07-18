package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.models.enums.EstadoPostulacion;
import proyecto.nuevaases.models.enums.Rol;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    List<Usuario> findByRolAndEstadoPostulacion(Rol rol, EstadoPostulacion estadoPostulacion);

    long countByRolAndEstadoPostulacion(Rol rol, EstadoPostulacion estadoPostulacion);

    long countByRolAndActivo(Rol rol, boolean activo);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findTop10ByEmailContainingOrNombreCompletoContaining(String email, String nombreCompleto);
}

