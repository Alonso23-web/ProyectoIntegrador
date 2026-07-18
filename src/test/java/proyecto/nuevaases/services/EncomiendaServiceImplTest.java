package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.EncomiendaDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.models.enums.EstadoEncomienda;
import proyecto.nuevaases.repositories.EncomiendaRepository;
import proyecto.nuevaases.services.IHistorialEncomiendaService;
import proyecto.nuevaases.services.impl.EncomiendaServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncomiendaServiceImplTest {

    @Mock
    private EncomiendaRepository encomiendaRepository;

    @Mock
    private IHistorialEncomiendaService historialEncomiendaService;

    @InjectMocks
    private EncomiendaServiceImpl encomiendaService;

    private EncomiendaDTO dto;
    private Encomienda entityReservado;

    @BeforeEach
    void setUp() {
        dto = EncomiendaDTO.builder()
                .id(10L)
                .codigoRastreo(null)
                .remitente("Juan")
                .dniRemitente("123")
                .destinatario("Maria")
                .dniDestinatario("456")
                .origen("Trujillo")
                .destino("Chepén")
                .descripcion("Caja")
                .peso(12.0)
                .precio(0.0)
                .fechaEnvio(LocalDate.now())
                .fechaEstimadaEntrega(LocalDate.now().plusDays(3))
                .estado("REGISTRADO")
                .observaciones("-")
                .creadoPorEmail("admin@example.com")
                .build();

        entityReservado = Encomienda.builder()
                .id(10L)
                .codigoRastreo("NAE-2024-1000")
                .remitente("Juan")
                .dniRemitente("123")
                .destinatario("Maria")
                .dniDestinatario("456")
                .origen("Trujillo")
                .destino("Chepén")
                .descripcion("Caja")
                .peso(10.0)
                .precio(0.0)
                .fechaEnvio(LocalDate.now())
                .fechaEstimadaEntrega(LocalDate.now().plusDays(3))
                .estado(EstadoEncomienda.REGISTRADO)
                .observaciones("-")
                .creadoPorEmail("admin@example.com")
                .build();
    }

    @Test
    void buscarPorId_conIdValido_debeRetornarDTO() {
        when(encomiendaRepository.findById(10L)).thenReturn(Optional.of(entityReservado));

        EncomiendaDTO resultado = encomiendaService.buscarPorId(10L);

        assertNotNull(resultado);
        assertEquals("NAE-2024-1000", resultado.getCodigoRastreo());
        verify(encomiendaRepository, times(1)).findById(10L);
    }

    @Test
    void buscarPorId_conIdInvalido_debeThrowResourceNotFound() {
        when(encomiendaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> encomiendaService.buscarPorId(999L));
        verify(encomiendaRepository, times(1)).findById(999L);
    }

    @Test
    void eliminar_conIdValido_debeEliminar() {
        when(encomiendaRepository.existsById(10L)).thenReturn(true);

        encomiendaService.eliminar(10L);

        verify(encomiendaRepository, times(1)).existsById(10L);
        verify(encomiendaRepository, times(1)).deleteById(10L);
    }

    @Test
    void eliminar_conIdInvalido_debeThrowResourceNotFound() {
        when(encomiendaRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> encomiendaService.eliminar(999L));
        verify(encomiendaRepository, never()).deleteById(anyLong());
    }

    @Test
    void guardar_conEncomiendaNueva_sinCodigoRastreo_debeGenerarCodigoYGuardar() {
        EncomiendaDTO input = EncomiendaDTO.builder()
                .id(null)
                .codigoRastreo(dto.getCodigoRastreo())
                .remitente(dto.getRemitente())
                .dniRemitente(dto.getDniRemitente())
                .destinatario(dto.getDestinatario())
                .dniDestinatario(dto.getDniDestinatario())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .descripcion(dto.getDescripcion())
                .peso(dto.getPeso())
                .precio(dto.getPrecio())
                .fechaEnvio(dto.getFechaEnvio())
                .fechaEstimadaEntrega(dto.getFechaEstimadaEntrega())
                .estado(dto.getEstado())
                .observaciones(dto.getObservaciones())
                .creadoPorEmail(dto.getCreadoPorEmail())
                .build();

        Encomienda guardada = Encomienda.builder()
                .id(50L)
                .codigoRastreo("NAE-2024-1234")
                .estado(EstadoEncomienda.REGISTRADO)
                .remitente("Juan")
                .dniRemitente("123")
                .destinatario("Maria")
                .dniDestinatario("456")
                .origen("Trujillo")
                .destino("Chepén")
                .descripcion("Caja")
                .peso(12.0)
                .precio(0.0)
                .fechaEnvio(LocalDate.now())
                .fechaEstimadaEntrega(LocalDate.now().plusDays(3))
                .observaciones("-")
                .creadoPorEmail("admin@example.com")
                .build();

        when(encomiendaRepository.save(any(Encomienda.class))).thenReturn(guardada);

        EncomiendaDTO resultado = encomiendaService.guardar(input);

        assertNotNull(resultado);
        assertEquals("NAE-2024-1234", resultado.getCodigoRastreo());
        verify(encomiendaRepository, times(1)).save(any(Encomienda.class));
        verifyNoInteractions(historialEncomiendaService);
    }

    @Test
    void guardar_conCambioEstado_debeRegistrarHistorial() {
        // Estado anterior en BD
        when(encomiendaRepository.findById(10L)).thenReturn(Optional.of(entityReservado));

        // Guardada cambia de estado: REGISTRADO -> ENTREGADO
        Encomienda guardada = Encomienda.builder()
                .id(entityReservado.getId())
                .codigoRastreo(entityReservado.getCodigoRastreo())
                .remitente(entityReservado.getRemitente())
                .dniRemitente(entityReservado.getDniRemitente())
                .destinatario(entityReservado.getDestinatario())
                .dniDestinatario(entityReservado.getDniDestinatario())
                .origen(entityReservado.getOrigen())
                .destino(entityReservado.getDestino())
                .descripcion(entityReservado.getDescripcion())
                .peso(entityReservado.getPeso())
                .precio(entityReservado.getPrecio())
                .fechaEnvio(entityReservado.getFechaEnvio())
                .fechaEstimadaEntrega(entityReservado.getFechaEstimadaEntrega())
                .estado(EstadoEncomienda.ENTREGADO)
                .observaciones(entityReservado.getObservaciones())
                .creadoPorEmail(entityReservado.getCreadoPorEmail())
                .build();
        when(encomiendaRepository.save(any(Encomienda.class))).thenReturn(guardada);

        EncomiendaDTO input = EncomiendaDTO.builder()
                .id(dto.getId())
                .codigoRastreo("NAE-2024-1000")
                .remitente(dto.getRemitente())
                .dniRemitente(dto.getDniRemitente())
                .destinatario(dto.getDestinatario())
                .dniDestinatario(dto.getDniDestinatario())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .descripcion(dto.getDescripcion())
                .peso(dto.getPeso())
                .precio(dto.getPrecio())
                .fechaEnvio(dto.getFechaEnvio())
                .fechaEstimadaEntrega(dto.getFechaEstimadaEntrega())
                .estado("ENTREGADO")
                .observaciones(dto.getObservaciones())
                .creadoPorEmail(dto.getCreadoPorEmail())
                .build();

        EncomiendaDTO resultado = encomiendaService.guardar(input);

        assertNotNull(resultado);
        assertEquals("ENTREGADO", resultado.getEstado());

        verify(historialEncomiendaService, times(1)).registrarCambio(
                eq(10L),
                eq("REGISTRADO"),
                eq("ENTREGADO"),
                eq("admin@example.com"),
                contains("Cambio de estado"));
    }

    @Test
    void buscarPorDni_conEstadoVacio_debeUsarMetodoSinEstado() {
        when(encomiendaRepository.findByDniRemitenteOrDniDestinatario(eq("123"), eq("123")))
                .thenReturn(List.of(entityReservado));

        List<EncomiendaDTO> res = encomiendaService.buscarPorDni("123", "");

        assertEquals(1, res.size());
        verify(encomiendaRepository, times(1))
                .findByDniRemitenteOrDniDestinatario("123", "123");
        verify(encomiendaRepository, never())
                .findByDniRemitenteOrDniDestinatarioAndEstado(anyString(), anyString(), any());
    }

    @Test
    void buscarPorDni_conEstadoValido_debeUsarMetodoConEstado() {
        when(encomiendaRepository.findByDniRemitenteOrDniDestinatarioAndEstado(
                eq("123"),
                eq("123"),
                eq(EstadoEncomienda.REGISTRADO))).thenReturn(List.of(entityReservado));

        List<EncomiendaDTO> res = encomiendaService.buscarPorDni("123", "REGISTRADO");

        assertEquals(1, res.size());
        verify(encomiendaRepository, times(1))
                .findByDniRemitenteOrDniDestinatarioAndEstado("123", "123", EstadoEncomienda.REGISTRADO);
    }

    @Test
    void calcularPrecio_conPesoSinCargoExtra_debeRetornarRedondeado() {
        // Trujillo-Chepén => 5.00, peso 10 (sin cargo), + manejo 2.00 => total 7.00
        double precio = encomiendaService.calcularPrecio("Trujillo", "Chepén", 10.0);
        assertEquals(7.00, precio);
    }

    @Test
    void calcularPrecio_conPesoConCargoExtra_debeRetornarRedondeado() {
        // Trujillo-Chepén => 5.00, peso 12 => (12-10)*1.50=3.00, manejo 2.00 => 10.00
        double precio = encomiendaService.calcularPrecio("Trujillo", "Chepén", 12.0);
        assertEquals(10.00, precio);
    }

    @Test
    void calcularPrecioPorPeso_debeMultiplicarYRedondear() {
        // 3 * 1.50 = 4.5
        assertEquals(4.50, encomiendaService.calcularPrecioPorPeso(3.0));
    }
}
