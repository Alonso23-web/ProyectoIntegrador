package proyecto.nuevaases.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.nuevaases.models.ContactoMensaje;

import java.util.List;

@Repository
public interface ContactoMensajeRepository extends JpaRepository<ContactoMensaje, Long> {
    List<ContactoMensaje> findAllByOrderByFechaCreacionDesc();
    long countByLeido(boolean leido);
}
