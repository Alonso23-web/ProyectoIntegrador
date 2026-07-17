package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.SolicitudAlquilerDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.SolicitudAlquiler;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.repositories.SolicitudAlquilerRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.services.impl.SolicitudAlquilerServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudAlquilerServiceImplTest {

    @Mock
    private SolicitudAlquilerRepository solicitudAlquilerRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private SolicitudAlquilerServiceImpl solicitudAlquilerService;

    private SolicitudAlquilerDTO dto;
    private SolicitudAlquiler entity;

    @BeforeEach
    void setUp() {
        dto = SolicitudAlquilerDTO.builder()
                .nombreSolicitante("Ricardo")
                .empresa("Nueva Ases")
                .telefono("987654321")
                .correo("ricardo@example.com")
                .tipoVehiculo("BUS")
                .fechaInicio(LocalDate.of(2026, 7, 1))
                .fechaFin(LocalDate.of(2026, 7, 5))
                .cantidadPersonas(20)
                .origen("Trujillo")
                .destino("Chiclayo")
                .precioReferencial(500.0)
                .horasPorDia(8)
                .estado("PENDIENTE")
                .build();

        entity = SolicitudAlquiler.builder()
                .id(1L)
                .nombreSolicitante("Ricardo")
                .empresa("Nueva Ases")
                .telefono("987654321")
                .correo("ricardo@example.com")
                .tipoVehiculo("BUS")
                .fechaInicio(LocalDate.of(2026, 7, 1))
                .fechaFin(LocalDate.of(2026, 7, 5))
                .cantidadPersonas(20)
                .origen("Trujillo")
                .destino("Chiclayo")
                .precioReferencial(500.0)
                .horasPorDia(8)
                .estado("PENDIENTE")
                .build();
    }

    @Test
    void guardar_sinVehiculo_debeCrearSolicitudConEstadoPendiente() {
        when(solicitudAlquilerRepository.save(any(SolicitudAlquiler.class))).thenReturn(entity);

        SolicitudAlquilerDTO resultado = solicitudAlquilerService.guardar(dto);

        assertNotNull(resultado);
        assertEquals("Ricardo", resultado.getNombreSolicitante());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("Nueva Ases", resultado.getEmpresa());
        assertEquals(20, resultado.getCantidadPersonas());
        assertEquals(500.0, resultado.getPrecioReferencial());
        verify(solicitudAlquilerRepository, times(1)).save(any(SolicitudAlquiler.class));
        verifyNoInteractions(vehiculoRepository);
    }

    @Test
    void guardar_conVehiculoId_debeAsignarVehiculo() {
        Vehiculo vehiculo = Vehiculo.builder()
                .id(10L).marca("Mercedes").modelo("Sprinter").placa("XYZ-789")
                .build();

        dto.setVehiculoId(10L);

        SolicitudAlquiler entityConVehiculo = SolicitudAlquiler.builder()
                .id(2L)
                .nombreSolicitante(dto.getNombreSolicitante())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .tipoVehiculo(dto.getTipoVehiculo())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .cantidadPersonas(dto.getCantidadPersonas())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .precioReferencial(dto.getPrecioReferencial())
                .estado("PENDIENTE")
                .vehiculo(vehiculo)
                .build();

        when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(vehiculo));
        when(solicitudAlquilerRepository.save(any(SolicitudAlquiler.class))).thenReturn(entityConVehiculo);

        SolicitudAlquilerDTO resultado = solicitudAlquilerService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getVehiculoId());
        assertTrue(resultado.getVehiculoInfo().contains("Mercedes"));
        verify(vehiculoRepository, times(1)).findById(10L);
        verify(solicitudAlquilerRepository, times(1)).save(any(SolicitudAlquiler.class));
    }

    @Test
    void guardar_conVehiculoIdInvalido_debeIgnorarVehiculo() {
        dto.setVehiculoId(999L);

        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());
        when(solicitudAlquilerRepository.save(any(SolicitudAlquiler.class))).thenReturn(entity);

        SolicitudAlquilerDTO resultado = solicitudAlquilerService.guardar(dto);

        assertNotNull(resultado);
        assertNull(resultado.getVehiculoInfo());
        verify(vehiculoRepository, times(1)).findById(999L);
    }

    @Test
    void guardar_conEstadoPersonalizado_debeUsarEseEstado() {
        dto.setEstado("CONTACTADO");
        SolicitudAlquiler entityContactado = SolicitudAlquiler.builder()
                .id(3L)
                .nombreSolicitante(dto.getNombreSolicitante())
                .estado("CONTACTADO")
                .build();

        when(solicitudAlquilerRepository.save(any(SolicitudAlquiler.class))).thenReturn(entityContactado);

        SolicitudAlquilerDTO resultado = solicitudAlquilerService.guardar(dto);

        assertEquals("CONTACTADO", resultado.getEstado());
    }

    @Test
    void listarTodos_debeRetornarTodasLasSolicitudes() {
        when(solicitudAlquilerRepository.findAllByOrderByFechaSolicitudDesc())
                .thenReturn(List.of(entity));

        List<SolicitudAlquilerDTO> resultado = solicitudAlquilerService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Ricardo", resultado.get(0).getNombreSolicitante());
    }

    @Test
    void buscarPorId_conIdValido_debeRetornar() {
        when(solicitudAlquilerRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<SolicitudAlquilerDTO> resultado = solicitudAlquilerService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Ricardo", resultado.get().getNombreSolicitante());
    }

    @Test
    void buscarPorId_conIdInvalido_debeRetornarVacio() {
        when(solicitudAlquilerRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<SolicitudAlquilerDTO> resultado = solicitudAlquilerService.buscarPorId(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void cambiarEstado_conIdValido_debeActualizarEstado() {
        when(solicitudAlquilerRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(solicitudAlquilerRepository.save(any(SolicitudAlquiler.class))).thenReturn(entity);

        solicitudAlquilerService.cambiarEstado(1L, "CONFIRMADO");

        verify(solicitudAlquilerRepository, times(1)).save(argThat(s ->
                "CONFIRMADO".equals(s.getEstado())
        ));
    }

    @Test
    void cambiarEstado_conIdInvalido_debeLanzarExcepcion() {
        when(solicitudAlquilerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> solicitudAlquilerService.cambiarEstado(999L, "CONFIRMADO"));
        verify(solicitudAlquilerRepository, never()).save(any());
    }

    @Test
    void contarPorEstado_debeRetornarConteo() {
        SolicitudAlquiler s1 = SolicitudAlquiler.builder().id(1L).estado("PENDIENTE").build();
        SolicitudAlquiler s2 = SolicitudAlquiler.builder().id(2L).estado("PENDIENTE").build();
        when(solicitudAlquilerRepository.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE"))
                .thenReturn(List.of(s1, s2));

        long conteo = solicitudAlquilerService.contarPorEstado("PENDIENTE");

        assertEquals(2L, conteo);
    }

    @Test
    void contarPorEstado_sinRegistros_debeRetornarCero() {
        when(solicitudAlquilerRepository.findByEstadoOrderByFechaSolicitudDesc("CONFIRMADO"))
                .thenReturn(List.of());

        long conteo = solicitudAlquilerService.contarPorEstado("CONFIRMADO");

        assertEquals(0L, conteo);
    }

    @Test
    void listarPorEstado_debeRetornarSoloDelEstado() {
        when(solicitudAlquilerRepository.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE"))
                .thenReturn(List.of(entity));

        List<SolicitudAlquilerDTO> resultado = solicitudAlquilerService.listarPorEstado("PENDIENTE");

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
    }

    @Test
    void eliminar_conIdValido_debeEliminar() {
        when(solicitudAlquilerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(solicitudAlquilerRepository).deleteById(1L);

        solicitudAlquilerService.eliminar(1L);

        verify(solicitudAlquilerRepository, times(1)).existsById(1L);
        verify(solicitudAlquilerRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_conIdInvalido_debeLanzarExcepcion() {
        when(solicitudAlquilerRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> solicitudAlquilerService.eliminar(999L));
        verify(solicitudAlquilerRepository, never()).deleteById(any());
    }
}
