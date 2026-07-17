package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.VehiculoDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.enums.EstadoVehiculo;
import proyecto.nuevaases.models.enums.TipoVehiculo;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.impl.VehiculoServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceImplTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VehiculoServiceImpl vehiculoService;

    private VehiculoDTO dto;
    private Vehiculo entity;

    @BeforeEach
    void setUp() {
        dto = VehiculoDTO.builder()
                .placa("ABC-123")
                .marca("Toyota")
                .modelo("Hiace")
                .anio(2022)
                .capacidad(15)
                .tipo("MINIVAN")
                .estado("DISPONIBLE")
                .precioPorDia(150.0)
                .descripcion("Vehículo en buen estado")
                .build();

        entity = Vehiculo.builder()
                .id(1L)
                .placa("ABC-123")
                .marca("Toyota")
                .modelo("Hiace")
                .anio(2022)
                .capacidad(15)
                .tipo(TipoVehiculo.MINIVAN)
                .estado(EstadoVehiculo.DISPONIBLE)
                .precioPorDia(150.0)
                .descripcion("Vehículo en buen estado")
                .tipoPropiedad("PROPIO")
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() {
        Vehiculo v2 = Vehiculo.builder()
                .id(2L).placa("XYZ-789").marca("Mercedes").tipo(TipoVehiculo.BUS)
                .estado(EstadoVehiculo.DISPONIBLE).precioPorDia(200.0).tipoPropiedad("PROPIO")
                .build();
        when(vehiculoRepository.findAll()).thenReturn(List.of(entity, v2));

        List<VehiculoDTO> resultado = vehiculoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("ABC-123", resultado.get(0).getPlaca());
        assertEquals("XYZ-789", resultado.get(1).getPlaca());
        verify(vehiculoRepository, times(1)).findAll();
    }

    @Test
    void listarTodos_sinVehiculos_debeRetornarListaVacia() {
        when(vehiculoRepository.findAll()).thenReturn(List.of());

        List<VehiculoDTO> resultado = vehiculoService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorId_conIdValido_debeRetornarDTO() {
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(entity));

        VehiculoDTO resultado = vehiculoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("ABC-123", resultado.getPlaca());
        assertEquals("Toyota", resultado.getMarca());
        assertEquals("Hiace", resultado.getModelo());
        assertEquals(2022, resultado.getAnio());
        assertEquals(15, resultado.getCapacidad());
        assertEquals("MINIVAN", resultado.getTipo());
        assertEquals("DISPONIBLE", resultado.getEstado());
        assertEquals(150.0, resultado.getPrecioPorDia());
    }

    @Test
    void buscarPorId_conIdInvalido_debeLanzarExcepcion() {
        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.buscarPorId(999L));
    }

    @Test
    void guardar_debeConvertirYGuardar() {
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(entity);

        VehiculoDTO resultado = vehiculoService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("ABC-123", resultado.getPlaca());
        assertEquals("Toyota", resultado.getMarca());
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    void guardar_conValoresNulos_debeUsarValoresPorDefecto() {
        VehiculoDTO dtoMinimo = VehiculoDTO.builder()
                .placa("MIN-001")
                .marca("Nissan")
                .modelo("Urvan")
                .build();

        Vehiculo entityMinimo = Vehiculo.builder()
                .id(5L)
                .placa("MIN-001")
                .marca("Nissan")
                .modelo("Urvan")
                .anio(0)
                .capacidad(0)
                .tipo(TipoVehiculo.BUS)
                .estado(EstadoVehiculo.DISPONIBLE)
                .precioPorDia(0.0)
                .tipoPropiedad("PROPIO")
                .build();

        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(entityMinimo);

        VehiculoDTO resultado = vehiculoService.guardar(dtoMinimo);

        assertNotNull(resultado);
        assertEquals("MIN-001", resultado.getPlaca());
        assertEquals(0, resultado.getAnio());
        assertEquals(0, resultado.getCapacidad());
        assertEquals("BUS", resultado.getTipo());
        assertEquals("DISPONIBLE", resultado.getEstado());
        assertEquals(0.0, resultado.getPrecioPorDia());
    }

    @Test
    void eliminar_conIdValido_debeEliminar() {
        when(vehiculoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehiculoRepository).deleteById(1L);

        vehiculoService.eliminar(1L);

        verify(vehiculoRepository, times(1)).existsById(1L);
        verify(vehiculoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_conIdInvalido_debeLanzarExcepcion() {
        when(vehiculoRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.eliminar(999L));
        verify(vehiculoRepository, never()).deleteById(any());
    }
}
