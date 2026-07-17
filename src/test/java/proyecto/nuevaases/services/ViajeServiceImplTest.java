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
import proyecto.nuevaases.repositories.EncomiendaRepository;
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

    @Mock
    private EncomiendaRepository encomiendaRepository;

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

    // ========================================================================
    // buscar (entity-based) — tests existentes
    // ========================================================================

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
    void buscar_conCantidadCeroDebeUsarMinimo1() {
        when(viajeRepository.findByOrigenAndDestinoAndFecha(
                eq("Trujillo"), eq("Chepén"), any(LocalDate.class)
        )).thenReturn(List.of(viaje1, viaje2));

        List<Viaje> res = viaService.buscar("Trujillo", "Chepén", LocalDate.of(2026, 1, 1), 0);

        assertEquals(2, res.size());
    }

    // ========================================================================
    // buscarDTO
    // ========================================================================

    @Test
    void buscarDTO_debeDelegarABuscarYConvertir() {
        when(viajeRepository.findByOrigenAndDestinoAndFecha(
                eq("Trujillo"), eq("Chepén"), any(LocalDate.class)
        )).thenReturn(List.of(viaje1));

        List<ViajeDTO> res = viaService.buscarDTO("Trujillo", "Chepén", LocalDate.of(2026, 1, 1), 2);

        assertEquals(1, res.size());
        assertEquals("Trujillo", res.get(0).getOrigen());
        assertEquals("Chepén", res.get(0).getDestino());
        assertEquals("PROGRAMADO", res.get(0).getEstadoViaje());
    }

    // ========================================================================
    // eliminarDTO
    // ========================================================================

    @Test
    void eliminarDTO_conIdInexistenteDebeThrow() {
        when(viajeRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> viaService.eliminarDTO(999L));
        verify(viajeRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarDTO_conIdValido_debeDesvincularEncomiendasYEliminarReservas() {
        when(viajeRepository.existsById(1L)).thenReturn(true);
        when(encomiendaRepository.desvincularEncomiendas(1L)).thenReturn(2);
        when(viajeRepository.eliminarReservasAsociadas(1L)).thenReturn(3);
        doNothing().when(viajeRepository).deleteById(1L);

        viaService.eliminarDTO(1L);

        verify(encomiendaRepository, times(1)).desvincularEncomiendas(1L);
        verify(viajeRepository, times(1)).eliminarReservasAsociadas(1L);
        verify(viajeRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarDTO_conIdValido_sinDatosAsociados_debeEliminarDirectamente() {
        when(viajeRepository.existsById(1L)).thenReturn(true);
        when(encomiendaRepository.desvincularEncomiendas(1L)).thenReturn(0);
        when(viajeRepository.eliminarReservasAsociadas(1L)).thenReturn(0);
        doNothing().when(viajeRepository).deleteById(1L);

        viaService.eliminarDTO(1L);

        verify(encomiendaRepository, times(1)).desvincularEncomiendas(1L);
        verify(viajeRepository, times(1)).eliminarReservasAsociadas(1L);
        verify(viajeRepository, times(1)).deleteById(1L);
    }

    // ========================================================================
    // guardarDTO
    // ========================================================================

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
                .estadoViaje(null)
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

    @Test
    void guardarDTO_sinVehiculoId_debeGuardarSinVehiculo() {
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
                .estadoViaje("EN_CURSO")
                .build();

        Viaje entitySaved = Viaje.builder()
                .id(200L)
                .origen(input.getOrigen())
                .destino(input.getDestino())
                .fecha(input.getFecha())
                .horaSalida(input.getHoraSalida())
                .tipoBus(input.getTipoBus())
                .totalAsientos(input.getTotalAsientos())
                .precio(input.getPrecio())
                .creadoPorEmail(input.getCreadoPorEmail())
                .conductorEmail(input.getConductorEmail())
                .estadoViaje(EstadoViaje.EN_CURSO)
                .build();

        when(viajeRepository.save(any(Viaje.class))).thenReturn(entitySaved);

        ViajeDTO res = viaService.guardarDTO(input);

        assertNotNull(res);
        assertEquals("EN_CURSO", res.getEstadoViaje());
        assertNull(res.getVehiculoInfo());
        verify(vehiculoRepository, never()).findById(anyLong());
    }

    // ========================================================================
    // obtenerPorIdDTO
    // ========================================================================

    @Test
    void obtenerPorIdDTO_conIdValido_debeRetornarDTO() {
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje1));

        Optional<ViajeDTO> res = viaService.obtenerPorIdDTO(1L);

        assertTrue(res.isPresent());
        assertEquals("Trujillo", res.get().getOrigen());
        assertEquals(20.0, res.get().getPrecio());
    }

    @Test
    void obtenerPorIdDTO_conIdInvalido_debeRetornarVacio() {
        when(viajeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ViajeDTO> res = viaService.obtenerPorIdDTO(999L);

        assertFalse(res.isPresent());
    }

    // ========================================================================
    // Business logic: generarMasivo
    // ========================================================================

    @Test
    void generarMasivo_unSoloDiaUnHorario_debeCrearUnViaje() {
        LocalDate fecha = LocalDate.of(2026, 2, 1);
        when(viajeRepository.findByOrigenAndDestinoAndFecha("A", "B", fecha))
                .thenReturn(List.of()); // no existe → se crea

        int creados = viaService.generarMasivo(
                fecha, fecha, "A", "B", false,
                List.of("10:00"), "BUS", 20, 25.0,
                "admin@x.com", null, null, "PROGRAMADO"
        );

        assertEquals(1, creados);
        verify(viajeRepository, times(1)).findByOrigenAndDestinoAndFecha("A", "B", fecha);
        verify(viajeRepository, times(1)).save(any(Viaje.class));
    }

    @Test
    void generarMasivo_conDuplicado_noDebeCrear() {
        LocalDate fecha = LocalDate.of(2026, 2, 1);
        // Ya existe un viaje con esa ruta, fecha y hora → no se debe crear duplicado
        Viaje existente = Viaje.builder()
                .id(99L)
                .origen("A").destino("B")
                .fecha(fecha).horaSalida("10:00")
                .build();
        when(viajeRepository.findByOrigenAndDestinoAndFecha("A", "B", fecha))
                .thenReturn(List.of(existente));

        int creados = viaService.generarMasivo(
                fecha, fecha, "A", "B", false,
                List.of("10:00"), "BUS", 20, 25.0,
                "admin@x.com", null, null, "PROGRAMADO"
        );

        assertEquals(0, creados);
        verify(viajeRepository, never()).save(any(Viaje.class));
    }

    @Test
    void generarMasivo_conInverso_debeCrearIdaYVuelta() {
        LocalDate fecha = LocalDate.of(2026, 2, 1);
        when(viajeRepository.findByOrigenAndDestinoAndFecha("A", "B", fecha))
                .thenReturn(List.of());
        when(viajeRepository.findByOrigenAndDestinoAndFecha("B", "A", fecha))
                .thenReturn(List.of());

        int creados = viaService.generarMasivo(
                fecha, fecha, "A", "B", true,
                List.of("10:00"), "BUS", 20, 25.0,
                "admin@x.com", null, null, "PROGRAMADO"
        );

        assertEquals(2, creados);
        verify(viajeRepository, times(2)).save(any(Viaje.class));
    }

    @Test
    void generarMasivo_rangoFechasMultiplesHorarios_debeCrearTodos() {
        LocalDate inicio = LocalDate.of(2026, 3, 1);
        LocalDate fin = LocalDate.of(2026, 3, 3);
        List<String> horarios = List.of("08:00", "16:00");

        // Sin inverso: 3 días × 2 horarios = 6 viajes
        when(viajeRepository.findByOrigenAndDestinoAndFecha(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(List.of());

        int creados = viaService.generarMasivo(
                inicio, fin, "A", "B", false,
                horarios, "MINIVAN", 15, 30.0,
                "admin@x.com", null, null, "PROGRAMADO"
        );

        assertEquals(6, creados);
        // 6 saves + 6 queries de verificación
        verify(viajeRepository, times(6)).save(any(Viaje.class));
        verify(viajeRepository, times(6))
                .findByOrigenAndDestinoAndFecha(anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    void generarMasivo_conConductorYVehiculo_debeAsignarlos() {
        LocalDate fecha = LocalDate.of(2026, 4, 1);
        Vehiculo vehiculo = Vehiculo.builder().id(10L).marca("Toyota").modelo("Hiace").build();

        when(viajeRepository.findByOrigenAndDestinoAndFecha("X", "Y", fecha))
                .thenReturn(List.of());
        when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(vehiculo));

        int creados = viaService.generarMasivo(
                fecha, fecha, "X", "Y", false,
                List.of("14:00"), "BUS", 30, 40.0,
                "admin@x.com", "conductor@mail.com", 10L, "PROGRAMADO"
        );

        assertEquals(1, creados);
        verify(vehiculoRepository, times(1)).findById(10L);
        verify(viajeRepository, times(1)).save(argThat(v ->
                "conductor@mail.com".equals(v.getConductorEmail()) &&
                v.getVehiculo() != null &&
                v.getVehiculo().getId() == 10L
        ));
    }

    @Test
    void generarMasivo_conVehiculoInexistente_debeCrearSinVehiculo() {
        LocalDate fecha = LocalDate.of(2026, 4, 1);

        when(viajeRepository.findByOrigenAndDestinoAndFecha("X", "Y", fecha))
                .thenReturn(List.of());
        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        int creados = viaService.generarMasivo(
                fecha, fecha, "X", "Y", false,
                List.of("14:00"), "BUS", 30, 40.0,
                "admin@x.com", "conductor@mail.com", 999L, "PROGRAMADO"
        );

        assertEquals(1, creados);
        verify(viajeRepository, times(1)).save(argThat(v ->
                "conductor@mail.com".equals(v.getConductorEmail()) &&
                v.getVehiculo() == null
        ));
    }

    // ========================================================================
    // Métodos entity-based (ViajeService interface)
    // ========================================================================

    @Test
    void listarTodos_entity_debeRetornarTodos() {
        when(viajeRepository.findAll()).thenReturn(List.of(viaje1, viaje2));

        List<Viaje> res = viaService.listarTodos();

        assertEquals(2, res.size());
    }

    @Test
    void listarTodosDTO_debeRetornarDTOs() {
        when(viajeRepository.findAll()).thenReturn(List.of(viaje1, viaje2));

        List<ViajeDTO> res = viaService.listarTodosDTO();

        assertEquals(2, res.size());
        assertEquals("Trujillo", res.get(0).getOrigen());
    }

    @Test
    void obtenerPorId_entity_conIdValido_debeRetornar() {
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje1));

        Optional<Viaje> res = viaService.obtenerPorId(1L);

        assertTrue(res.isPresent());
        assertEquals("Trujillo", res.get().getOrigen());
    }

    @Test
    void guardar_entity_debeGuardar() {
        viaService.guardar(viaje1);

        verify(viajeRepository, times(1)).save(viaje1);
    }

    @Test
    void eliminar_entity_debeEliminar() {
        viaService.eliminar(1L);

        verify(viajeRepository, times(1)).deleteById(1L);
    }
}
