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

import java.util.List;
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
                .rol(Rol.CLIENTE)
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

    // ========================================================================
    // Business logic: existeDni
    // ========================================================================

    @Test
    void existeDni_conDniExistente_debeRetornarTrue() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(true);

        boolean existe = usuarioService.existeDni("12345678");

        assertTrue(existe);
        verify(usuarioRepository, times(1)).existsByDni("12345678");
    }

    @Test
    void existeDni_conDniNoExistente_debeRetornarFalse() {
        when(usuarioRepository.existsByDni("00000000")).thenReturn(false);

        boolean existe = usuarioService.existeDni("00000000");

        assertFalse(existe);
    }

    // ========================================================================
    // Business logic: cambiarEstadoActivo
    // ========================================================================

    @Test
    void cambiarEstadoActivo_conIdValido_debeCambiarActivo() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("user@example.com")
                .activo(true)
                .rol(Rol.CLIENTE)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioDTO resultado = usuarioService.cambiarEstadoActivo(1L, false);

        assertNotNull(resultado);
        assertFalse(resultado.isActivo());
        verify(usuarioRepository, times(1)).save(argThat(u -> !u.isActivo()));
    }

    @Test
    void cambiarEstadoActivo_conIdInvalido_debeLanzarExcepcion() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.cambiarEstadoActivo(999L, true));
        verify(usuarioRepository, never()).save(any());
    }

    // ========================================================================
    // Business logic: conductores
    // ========================================================================

    @Test
    void contarConductoresActivos_debeRetornarConteo() {
        when(usuarioRepository.countByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.APROBADO))
                .thenReturn(5L);

        long conteo = usuarioService.contarConductoresActivos();

        assertEquals(5L, conteo);
        verify(usuarioRepository, times(1))
                .countByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.APROBADO);
    }

    @Test
    void contarConductoresActivos_sinConductores_debeRetornarCero() {
        when(usuarioRepository.countByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.APROBADO))
                .thenReturn(0L);

        long conteo = usuarioService.contarConductoresActivos();

        assertEquals(0L, conteo);
    }

    @Test
    void listarConductoresPendientes_debeRetornarSoloPendientes() {
        Usuario pendiente1 = Usuario.builder()
                .id(1L).email("cond1@x.com").nombreCompleto("Conductor 1")
                .rol(Rol.CONDUCTOR).estadoPostulacion(EstadoPostulacion.PENDIENTE).build();
        Usuario pendiente2 = Usuario.builder()
                .id(2L).email("cond2@x.com").nombreCompleto("Conductor 2")
                .rol(Rol.CONDUCTOR).estadoPostulacion(EstadoPostulacion.PENDIENTE).build();

        when(usuarioRepository.findByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.PENDIENTE))
                .thenReturn(List.of(pendiente1, pendiente2));

        List<UsuarioDTO> resultados = usuarioService.listarConductoresPendientes();

        assertEquals(2, resultados.size());
        assertEquals("Conductor 1", resultados.get(0).getNombreCompleto());
        verify(usuarioRepository, times(1))
                .findByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.PENDIENTE);
    }

    @Test
    void listarConductoresPendientes_sinPendientes_debeRetornarListaVacia() {
        when(usuarioRepository.findByRolAndEstadoPostulacion(Rol.CONDUCTOR, EstadoPostulacion.PENDIENTE))
                .thenReturn(List.of());

        List<UsuarioDTO> resultados = usuarioService.listarConductoresPendientes();

        assertTrue(resultados.isEmpty());
    }

    @Test
    void listarConductores_debeRetornarTodosLosConductores() {
        Usuario cond1 = Usuario.builder().id(1L).email("c1@x.com").rol(Rol.CONDUCTOR).build();
        Usuario cond2 = Usuario.builder().id(2L).email("c2@x.com").rol(Rol.CONDUCTOR).build();

        when(usuarioRepository.findByRol(Rol.CONDUCTOR)).thenReturn(List.of(cond1, cond2));

        List<UsuarioDTO> resultados = usuarioService.listarConductores();

        assertEquals(2, resultados.size());
    }

    // ========================================================================
    // eliminar
    // ========================================================================

    @Test
    void eliminar_conIdValido_debeEliminar() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.eliminar(1L);

        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_conIdInvalido_debeLanzarExcepcion() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.eliminar(999L));
        verify(usuarioRepository, never()).deleteById(any());
    }
}
