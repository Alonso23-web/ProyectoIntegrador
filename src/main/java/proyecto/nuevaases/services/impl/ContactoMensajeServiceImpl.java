package proyecto.nuevaases.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.nuevaases.models.ContactoMensaje;
import proyecto.nuevaases.repositories.ContactoMensajeRepository;
import proyecto.nuevaases.services.IContactoMensajeService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactoMensajeServiceImpl implements IContactoMensajeService {

    private final ContactoMensajeRepository repository;

    @Override
    public ContactoMensaje guardar(String nombre, String correo, String telefono, String mensaje) {
        ContactoMensaje entity = ContactoMensaje.builder()
                .nombre(nombre)
                .correo(correo)
                .telefono(telefono)
                .mensaje(mensaje)
                .leido(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
        return repository.save(entity);
    }

    @Override
    public List<ContactoMensaje> listarTodos() {
        return repository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public ContactoMensaje marcarComoLeido(Long id) {
        ContactoMensaje msg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado con ID: " + id));
        msg.setLeido(true);
        return repository.save(msg);
    }

    @Override
    public long contarNoLeidos() {
        return repository.countByLeido(false);
    }
}
