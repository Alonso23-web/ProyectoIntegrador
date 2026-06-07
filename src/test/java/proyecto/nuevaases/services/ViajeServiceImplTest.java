package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.ViajeDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.impl.ViajeServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViajeServiceImplTest {

    @Mock
    private ViajeRepository viajeRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private ViajeServiceImpl viaService;

    private Viaje viaje1;
    private Viaje viaje2;

    @BeforeEach
    void setUp() {
        viaje1 = Viaje.builder()
                .id(1L)
                .origen("Trujillo")
                .destino("Chepén")
                .fecha(LocalDate.of(2026, 1, 1))
                .horaSalida("08:00")
                .tipoBus("MINIVAN")
                .totalAsientos(4)
                .precio(20.0)
                .conductorEmail("cond@x.com")
                .estadoViaje(EstadoViaje.PROGRAMADO)
                .creadoPorEmail("admin@x.com")
                .build();

        viaje2 = Viaje.builder()
                .id(2L)
                .origen("Trujillo")
                .destino("Chepén")
                .fecha(LocalDate.of(2026, 1, 1))
                .horaSalida("08:00")
                .tipoBus("MINIVAN")
                .totalAsientos(2)
                .precio(20.0)
                .conductorEmail("cond@x.com")
                .estadoViaje(EstadoViaje.PROGRAMADO)
                .creadoPorEmail("admin@x.com")
                .build();
    }

    @Test
    void buscar_conCantidadPasajerosDebeFiltrarPorAsientos() {
        when(viajeRepository.findByOrigenAndDestinoAndFecha(
                eq("Trujillo"), eq("Chepén"), any(LocalDate.class)
        )).thenReturn(List.of(viaje1, viaje2));

        List<Viaje> res = viaService.buscar("Trujillo", "Chepén", LocalDate.of(2026, 1, 1), 3);

        assertEquals(1, res.size());
        assertEquals(1L, res.get(0).getId());
    }

    @Test
    void buscar_conCantidadNullDebeUsarMinimo1() {
        when(viajeRepository.findByOrigenAndDestinoAndFecha(
                eq("Trujillo"), eq("Chepén"), any(LocalDate.class)
        )).thenReturn(List.of(viaje1, viaje2));

        List<Viaje> res = viaService.buscar("Trujillo", "Chepén", LocalDate.of(2026, 1, 1), null);

        assertEquals(2, res.size());
    }

    @Test
    void eliminarDTO_conIdInexistenteDebeThrow() {
        when(viajeRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> viaService.eliminarDTO(999L));
        verify(viajeRepository, never()).deleteById(anyLong());
    }

    @Test
    void guardarDTO_conVehiculoIdDebeSetearVehiculoEnEntity() {
        Vehiculo vehiculo = Vehiculo.builder()
                .id(55L)
                .marca("Ford")
                .modelo("Transit")
                .placa("AAA-111")
                .build();

        ViajeDTO input = ViajeDTO.builder()
                .origen("Trujillo")
                .destino("Chepén")
                .fecha(LocalDate.of(2026, 1, 1))
                .horaSalida("08:00")
                .tipoBus("MINIVAN")
                .totalAsientos(10)
                .precio(30.0)
                .creadoPorEmail("admin@x.com")
                .conductorEmail("cond@x.com")
                .estadoViaje(null) // debe usar PROGRAMADO
                .vehiculoId(55L)
                .build();

        when(vehiculoRepository.findById(55L)).thenReturn(Optional.of(vehiculo));

        Viaje entitySaved = Viaje.builder()
                .id(100L)
                .origen(input.getOrigen())
                .destino(input.getDestino())
                .fecha(input.getFecha())
                .horaSalida(input.getHoraSalida())
                .tipoBus(input.getTipoBus())
                .totalAsientos(input.getTotalAsientos())
                .precio(input.getPrecio())
                .creadoPorEmail(input.getCreadoPorEmail())
                .conductorEmail(input.getConductorEmail())
                .estadoViaje(EstadoViaje.PROGRAMADO)
                .vehiculo(vehiculo)
                .build();

        when(viajeRepository.save(any(Viaje.class))).thenReturn(entitySaved);

        ViajeDTO res = viaService.guardarDTO(input);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("PROGRAMADO", res.getEstadoViaje());
        assertEquals(55L, res.getVehiculoId());
        assertTrue(res.getVehiculoInfo().contains("Ford"));
        verify(vehiculoRepository, times(1)).findById(55L);
        verify(viajeRepository, times(1)).save(any(Viaje.class));
    }
}

