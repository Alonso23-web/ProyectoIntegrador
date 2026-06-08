package proyecto.nuevaases.services;

import proyecto.nuevaases.models.ContactoMensaje;

import java.util.List;

public interface IContactoMensajeService {
    ContactoMensaje guardar(String nombre, String correo, String telefono, String mensaje);
    List<ContactoMensaje> listarTodos();
    ContactoMensaje marcarComoLeido(Long id);
    long contarNoLeidos();
}
