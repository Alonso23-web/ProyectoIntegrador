package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    List<Usuario> findByRolAndEstadoPostulacion(String rol, String estadoPostulacion);

    long countByRolAndEstadoPostulacion(String rol, String estadoPostulacion);

    long countByRolAndActivo(String rol, boolean activo);

    List<Usuario> findByRol(String rol);
}

