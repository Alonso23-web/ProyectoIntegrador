package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import proyecto.nuevaases.dto.UsuarioDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.models.enums.EstadoPostulacion;
import proyecto.nuevaases.models.enums.Rol;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.services.impl.UsuarioServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        usuarioDTO = UsuarioDTO.builder()
                .email("test@example.com")
                .nombreCompleto("Test User")
                .dni("12345678")
                .rol(Rol.CLIENTE.name())
                .build();


    }

    @Test
    void registrar_conPasswordValido_debeEncriptarYGuardar() {
        // Arrange
        String password = "password123";
        String encryptedPassword = "$2a$10$encrypted";
        Usuario usuarioGuardado = Usuario.builder()
                .id(1L)
                .email("test@example.com")
                .nombreCompleto("Test User")
                .dni("12345678")
                .password(encryptedPassword)
                .activo(true)
                .rol(Rol.CLIENTE)
                .build();

        when(passwordEncoder.encode(password)).thenReturn(encryptedPassword);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        UsuarioDTO resultado = usuarioService.registrar(usuarioDTO, password);

        // Assert
        assertNotNull(resultado);
        assertEquals("test@example.com", resultado.getEmail());
        verify(passwordEncoder, times(1)).encode(password);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrar_sinPassword_debeEncriptarVacio() {
        // Arrange
        Usuario usuarioGuardado = Usuario.builder()
                .id(1L)
                .email("test@example.com")
                .password("")
                .activo(true)
                .build();

        when(passwordEncoder.encode("")).thenReturn("");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        UsuarioDTO resultado = usuarioService.registrar(usuarioDTO, null);

        // Assert
        assertNotNull(resultado);
        verify(passwordEncoder, times(1)).encode("");
    }

    @Test
    void aprobarConductor_conIdValido_debeActualizarEstado() {
        // Arrange
        Long conductorId = 1L;
        Usuario conductor = Usuario.builder()
                .id(conductorId)
                .email("conductor@example.com")
                .rol(Rol.CONDUCTOR)
                .estadoPostulacion(EstadoPostulacion.PENDIENTE)
                .activo(false)
                .build();

        when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(conductor);

        // Act
        usuarioService.aprobarConductor(conductorId);

        // Assert
        verify(usuarioRepository, times(1)).findById(conductorId);
        verify(usuarioRepository, times(1))
                .save(argThat(u -> u.getEstadoPostulacion() == EstadoPostulacion.APROBADO && u.isActivo()));

    }

    @Test
    void aprobarConductor_conIdInvalido_debeThrowException() {
        // Arrange
        Long conductorId = 999L;
        when(usuarioRepository.findById(conductorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.aprobarConductor(conductorId));
    }

    @Test
    void rechazarConductor_conIdValido_debeActualizarEstadoYDesactivar() {
        // Arrange
        Long conductorId = 2L;
        Usuario conductor = Usuario.builder()
                .id(conductorId)
                .email("conductor2@example.com")
                .rol(Rol.CONDUCTOR)
                .estadoPostulacion(EstadoPostulacion.PENDIENTE)
                .activo(true)
                .build();

        when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(conductor);

        // Act
        usuarioService.rechazarConductor(conductorId);

        // Assert
        verify(usuarioRepository, times(1))
                .save(argThat(u -> u.getEstadoPostulacion() == EstadoPostulacion.RECHAZADO && !u.isActivo()));

    }

    @Test
    void existeEmail_conEmailExistente_debeRetornarTrue() {
        // Arrange
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean existe = usuarioService.existeEmail("test@example.com");

        // Assert
        assertTrue(existe);
    }

    @Test
    void existeEmail_conEmailNoExistente_debeRetornarFalse() {
        // Arrange
        when(usuarioRepository.existsByEmail("noexiste@example.com")).thenReturn(false);

        // Act
        boolean existe = usuarioService.existeEmail("noexiste@example.com");

        // Assert
        assertFalse(existe);
    }
}
