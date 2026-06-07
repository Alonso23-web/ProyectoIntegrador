package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.services.impl.PasajeServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasajeServiceImplTest {

    @Mock
    private PasajeRepository pasajeRepository;

    @InjectMocks
    private PasajeServiceImpl pasajeService;

    private PasajeDTO pasajeDTO;
    private Pasaje pasaje;

    @BeforeEach
    void setUp() {
        pasajeDTO = PasajeDTO.builder()
                .id(1L)
                .nombrePasajero("Juan Pérez")
                .dni("12345678")
                .origen("Buenos Aires")
                .destino("Córdoba")
                .fechaViaje(LocalDate.now())
                .horaViaje("10:30")
                .asiento(5)
                .precio(500.0)
                .estado("CONFIRMADO")
                .creadoPorEmail("admin@example.com")
                .build();

        pasaje = Pasaje.builder()
                .id(1L)
                .nombrePasajero("Juan Pérez")
                .dni("12345678")
                .origen("Buenos Aires")
                .destino("Córdoba")
                .fechaViaje(LocalDate.now())
                .horaViaje("10:30")
                .asiento(5)
                .precio(500.0)
                .estado("CONFIRMADO")
                .creadoPorEmail("admin@example.com")
                .build();
    }

    @Test
    void listarTodos_debeRetornarListaDePasajes() {
        // Arrange
        Pasaje p2 = Pasaje.builder().id(2L).nombrePasajero("María").build();
        when(pasajeRepository.findAll()).thenReturn(List.of(pasaje, p2));

        // Act
        List<PasajeDTO> resultado = pasajeService.listarTodos();

        // Assert
        assertEquals(2, resultado.size());
        verify(pasajeRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_conIdValido_debeRetornarPasaje() {
        // Arrange
        when(pasajeRepository.findById(1L)).thenReturn(Optional.of(pasaje));

        // Act
        PasajeDTO resultado = pasajeService.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombrePasajero());
        assertEquals("12345678", resultado.getDni());
        verify(pasajeRepository, times(1)).findById(1L);
    }

    @Test
    void buscarPorId_conIdInvalido_debeThrowException() {
        // Arrange
        when(pasajeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pasajeService.buscarPorId(999L));
    }

    @Test
    void guardar_conPasajeValido_debeGuardar() {
        // Arrange
        when(pasajeRepository.save(any(Pasaje.class))).thenReturn(pasaje);

        // Act
        PasajeDTO resultado = pasajeService.guardar(pasajeDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombrePasajero());
        verify(pasajeRepository, times(1)).save(any(Pasaje.class));
    }

    @Test
    void eliminar_conIdValido_debeEliminarPasaje() {
        // Arrange
        when(pasajeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pasajeRepository).deleteById(1L);

        // Act
        pasajeService.eliminar(1L);

        // Assert
        verify(pasajeRepository, times(1)).existsById(1L);
        verify(pasajeRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_conIdInvalido_debeThrowException() {
        // Arrange
        when(pasajeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pasajeService.eliminar(999L));
        verify(pasajeRepository, never()).deleteById(any());
    }

    @Test
    void guardar_conPrecioNegativo_debeGuardarSinValidar() {
        // Este test verifica que el servicio no valida precios negativos
        // (validación que probablemente debería estar en el DTOo en la capa de
        // controlador)
        PasajeDTO pasajeNegativo = PasajeDTO.builder()
                .nombrePasajero("Test")
                .precio(-100.0)
                .build();

        Pasaje pasajeGuardado = Pasaje.builder()
                .id(1L)
                .nombrePasajero("Test")
                .precio(-100.0)
                .build();

        when(pasajeRepository.save(any(Pasaje.class))).thenReturn(pasajeGuardado);

        // Act
        PasajeDTO resultado = pasajeService.guardar(pasajeNegativo);

        // Assert
        assertEquals(-100.0, resultado.getPrecio());
        verify(pasajeRepository, times(1)).save(any(Pasaje.class));
    }

    @Test
    void listarTodos_sinPasajes_debeRetornarListaVacia() {
        // Arrange
        when(pasajeRepository.findAll()).thenReturn(List.of());

        // Act
        List<PasajeDTO> resultado = pasajeService.listarTodos();

        // Assert
        assertTrue(resultado.isEmpty());
    }
}
