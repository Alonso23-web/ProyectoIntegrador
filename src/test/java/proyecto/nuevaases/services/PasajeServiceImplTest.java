package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.PasajeDTO;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.exception.ResourceNotFoundException;
import proyecto.nuevaases.models.Pasaje;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoPasaje;
import proyecto.nuevaases.models.enums.EstadoViaje;
import proyecto.nuevaases.repositories.PasajeRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.impl.PasajeServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasajeServiceImplTest {

    @Mock
    private PasajeRepository pasajeRepository;

    @Mock
    private ViajeRepository viajeRepository;

    @InjectMocks
    private PasajeServiceImpl pasajeService;

    private PasajeDTO pasajeDTO;
    private Pasaje pasaje;
    private Viaje viaje;

    @BeforeEach
    void setUp() {
        viaje = Viaje.builder()
                .id(10L)
                .origen("Trujillo")
                .destino("Chepén")
                .fecha(LocalDate.now())
                .horaSalida("10:30")
                .tipoBus("MINIVAN")
                .totalAsientos(10)
                .precio(50.0)
                .estadoViaje(EstadoViaje.PROGRAMADO)
                .build();

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
                .estado("RESERVADO")
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
                .estado(EstadoPasaje.RESERVADO)
                .creadoPorEmail("admin@example.com")
                .build();
    }

    // ========================================================================
    // CRUD tests (existing)
    // ========================================================================

    @Test
    void listarTodos_debeRetornarListaDePasajes() {
        Pasaje p2 = Pasaje.builder()
                .id(2L)
                .nombrePasajero("María")
                .estado(EstadoPasaje.RESERVADO)
                .build();
        when(pasajeRepository.findAll()).thenReturn(List.of(pasaje, p2));

        List<PasajeDTO> resultado = pasajeService.listarTodos();

        assertEquals(2, resultado.size());
        verify(pasajeRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_conIdValido_debeRetornarPasaje() {
        when(pasajeRepository.findById(1L)).thenReturn(Optional.of(pasaje));

        PasajeDTO resultado = pasajeService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombrePasajero());
        assertEquals("12345678", resultado.getDni());
        verify(pasajeRepository, times(1)).findById(1L);
    }

    @Test
    void buscarPorId_conIdInvalido_debeThrowException() {
        when(pasajeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pasajeService.buscarPorId(999L));
    }

    @Test
    void guardar_conPasajeValido_debeGuardar() {
        when(pasajeRepository.findById(1L)).thenReturn(Optional.of(pasaje));
        when(pasajeRepository.save(any(Pasaje.class))).thenReturn(pasaje);

        PasajeDTO resultado = pasajeService.guardar(pasajeDTO);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombrePasajero());
        verify(pasajeRepository, times(1)).save(any(Pasaje.class));
    }

    @Test
    void eliminar_conIdValido_debeEliminarPasaje() {
        when(pasajeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pasajeRepository).deleteById(1L);

        pasajeService.eliminar(1L);

        verify(pasajeRepository, times(1)).existsById(1L);
        verify(pasajeRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_conIdInvalido_debeThrowException() {
        when(pasajeRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pasajeService.eliminar(999L));
        verify(pasajeRepository, never()).deleteById(any());
    }

    @Test
    void guardar_conPrecioNegativo_debeGuardarSinValidar() {
        PasajeDTO pasajeNegativo = PasajeDTO.builder()
                .nombrePasajero("Test")
                .precio(-100.0)
                .estado("RESERVADO")
                .build();

        Pasaje pasajeGuardado = Pasaje.builder()
                .id(1L)
                .nombrePasajero("Test")
                .precio(-100.0)
                .estado(EstadoPasaje.RESERVADO)
                .build();

        when(pasajeRepository.save(any(Pasaje.class))).thenReturn(pasajeGuardado);

        PasajeDTO resultado = pasajeService.guardar(pasajeNegativo);

        assertEquals(-100.0, resultado.getPrecio());
        verify(pasajeRepository, times(1)).save(any(Pasaje.class));
    }

    @Test
    void listarTodos_sinPasajes_debeRetornarListaVacia() {
        when(pasajeRepository.findAll()).thenReturn(List.of());

        List<PasajeDTO> resultado = pasajeService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    // ========================================================================
    // Business logic: guardar con viajeId (asignación desde viaje)
    // ========================================================================

    @Test
    void guardar_conViajeId_debeAsignarOrigenYDestinoDelViaje() {
        PasajeDTO dtoConViaje = PasajeDTO.builder()
                .nombrePasajero("Carlos")
                .dni("87654321")
                .asiento(3)
                .precio(50.0)
                .estado("RESERVADO")
                .viajeId(10L)
                .build();

        Pasaje pasajeGuardado = Pasaje.builder()
                .id(20L)
                .nombrePasajero("Carlos")
                .dni("87654321")
                .origen("Trujillo")
                .destino("Chepén")
                .fechaViaje(LocalDate.now())
                .horaViaje("10:30")
                .asiento(3)
                .precio(50.0)
                .estado(EstadoPasaje.RESERVADO)
                .viaje(viaje)
                .build();

        when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje));
        when(pasajeRepository.save(any(Pasaje.class))).thenReturn(pasajeGuardado);

        PasajeDTO resultado = pasajeService.guardar(dtoConViaje);

        assertNotNull(resultado);
        assertEquals("Trujillo", resultado.getOrigen());
        assertEquals("Chepén", resultado.getDestino());
        assertEquals(LocalDate.now(), resultado.getFechaViaje());
        assertEquals("10:30", resultado.getHoraViaje());
        verify(viajeRepository, times(1)).findById(10L);
    }

    @Test
    void guardar_conViajeIdInvalido_debeLanzarExcepcion() {
        PasajeDTO dto = PasajeDTO.builder()
                .nombrePasajero("Carlos")
                .viajeId(999L)
                .build();

        when(viajeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pasajeService.guardar(dto));
        verify(pasajeRepository, never()).save(any());
    }

    // ========================================================================
    // Business logic: asientosOcupados
    // ========================================================================

    @Test
    void asientosOcupados_porViajeEntity_debeRetornarAsientos() {
        Pasaje p1 = Pasaje.builder().id(1L).asiento(2).build();
        Pasaje p2 = Pasaje.builder().id(2L).asiento(5).build();
        when(pasajeRepository.findByViajeAndEstadoIn(viaje,
                List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO)))
                .thenReturn(List.of(p1, p2));

        List<Integer> asientos = pasajeService.asientosOcupados(viaje);

        assertEquals(2, asientos.size());
        assertTrue(asientos.contains(2));
        assertTrue(asientos.contains(5));
    }

    @Test
    void asientosOcupados_porViajeId_debeRetornarAsientos() {
        when(pasajeRepository.findAsientosByViajeId(eq(10L),
                eq(List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO))))
                .thenReturn(List.of(1, 3, 7));

        List<Integer> asientos = pasajeService.asientosOcupados(10L);

        assertEquals(3, asientos.size());
        assertTrue(asientos.contains(1));
        assertTrue(asientos.contains(7));
    }

    // ========================================================================
    // Business logic: reservar
    // ========================================================================

    @Test
    void reservar_debeCrearPasajeConCodigoBoletoYEstadoReservado() {
        when(pasajeRepository.save(any(Pasaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pasaje resultado = pasajeService.reservar(
                "cliente@mail.com", viaje, 4, "Ana López", "11122233");

        assertNotNull(resultado);
        assertEquals("cliente@mail.com", resultado.getUsuarioEmail());
        assertEquals(viaje, resultado.getViaje());
        assertEquals(4, resultado.getAsiento());
        assertEquals(EstadoPasaje.RESERVADO, resultado.getEstado());
        assertEquals("Ana López", resultado.getNombrePasajero());
        assertEquals("11122233", resultado.getDni());
        assertEquals("Trujillo", resultado.getOrigen());
        assertEquals("Chepén", resultado.getDestino());
        assertEquals(viaje.getFecha(), resultado.getFechaViaje());
        assertEquals("10:30", resultado.getHoraViaje());
        assertEquals(50.0, resultado.getPrecio());
        // El código de boleto debe comenzar con "B"
        assertTrue(resultado.getCodigoBoleto().startsWith("B"));
        assertTrue(resultado.getCodigoBoleto().length() > 1);
        verify(pasajeRepository, times(1)).save(any(Pasaje.class));
    }

    // ========================================================================
    // Business logic: reservarDTO
    // ========================================================================

    @Test
    void reservarDTO_conViajeValido_debeCrearReservaYRetornarDTO() {
        when(viajeRepository.findById(10L)).thenReturn(Optional.of(viaje));
        when(pasajeRepository.save(any(Pasaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PasajeDTO resultado = pasajeService.reservarDTO(
                "cliente@mail.com", 10L, 6, "Pedro", "99988877");

        assertNotNull(resultado);
        assertEquals("cliente@mail.com", resultado.getUsuarioEmail());
        assertEquals(6, resultado.getAsiento());
        assertEquals("RESERVADO", resultado.getEstado());
        assertEquals("Pedro", resultado.getNombrePasajero());
        assertEquals("Trujillo", resultado.getOrigen());
        assertEquals("Chepén", resultado.getDestino());
        assertTrue(resultado.getCodigoBoleto().startsWith("B"));
        verify(viajeRepository, times(1)).findById(10L);
        verify(pasajeRepository, times(1)).save(any(Pasaje.class));
    }

    @Test
    void reservarDTO_conViajeInvalido_debeLanzarExcepcion() {
        when(viajeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> pasajeService.reservarDTO("x@x.com", 999L, 1, "Nadie", "0000"));
        verify(pasajeRepository, never()).save(any());
    }

    // ========================================================================
    // Business logic: reservarMultiples
    // ========================================================================

    @Test
    void reservarMultiples_conExito_debeCrearTodosLosPasajes() {
        List<PasajeroReservaDTO> pasajeros = List.of(
                new PasajeroReservaDTO("Luis", "111"),
                new PasajeroReservaDTO("Marta", "222"),
                new PasajeroReservaDTO("Sofía", "333")
        );

        // Viaje tiene 10 asientos, 0 ocupados → 10 disponibles
        when(pasajeRepository.countByViajeAndEstadoIn(viaje,
                List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO)))
                .thenReturn(0L);
        when(pasajeRepository.save(any(Pasaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Pasaje> resultados = pasajeService.reservarMultiples("cliente@mail.com", viaje, pasajeros);

        assertEquals(3, resultados.size());
        // Todos deben tener asiento = 0 (sin asignar)
        assertTrue(resultados.stream().allMatch(p -> p.getAsiento() == 0));
        // Todos deben tener el código de boleto empezando con "B"
        assertTrue(resultados.stream().allMatch(p -> p.getCodigoBoleto().startsWith("B")));
        // Todos deben tener el estado RESERVADO
        assertTrue(resultados.stream().allMatch(p -> p.getEstado() == EstadoPasaje.RESERVADO));
        verify(pasajeRepository, times(3)).save(any(Pasaje.class));
    }

    @Test
    void reservarMultiples_sinPasajeros_debeLanzarExcepcion() {
        List<PasajeroReservaDTO> pasajeros = List.of();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pasajeService.reservarMultiples("cliente@mail.com", viaje, pasajeros));
        assertEquals("Debes indicar pasajeros", ex.getMessage());
        verify(pasajeRepository, never()).save(any());
    }

    @Test
    void reservarMultiples_conPasajerosNull_debeLanzarExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pasajeService.reservarMultiples("cliente@mail.com", viaje, null));
        assertEquals("Debes indicar pasajeros", ex.getMessage());
        verify(pasajeRepository, never()).save(any());
    }

    @Test
    void reservarMultiples_conCuposInsuficientes_debeLanzarExcepcion() {
        List<PasajeroReservaDTO> pasajeros = List.of(
                new PasajeroReservaDTO("Luis", "111"),
                new PasajeroReservaDTO("Marta", "222"),
                new PasajeroReservaDTO("Sofía", "333")
        );

        // Viaje tiene 10 asientos, 9 ocupados → solo 1 disponible
        when(pasajeRepository.countByViajeAndEstadoIn(viaje,
                List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO)))
                .thenReturn(9L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pasajeService.reservarMultiples("cliente@mail.com", viaje, pasajeros));
        assertTrue(ex.getMessage().contains("No hay suficientes cupos"));
        assertTrue(ex.getMessage().contains("1"));
        verify(pasajeRepository, never()).save(any());
    }

    // ========================================================================
    // Business logic: misPasajes / obtenerPasajesDTO
    // ========================================================================

    @Test
    void misPasajes_debeRetornarSoloPasajesDelUsuario() {
        Pasaje p1 = Pasaje.builder().id(1L).usuarioEmail("user@mail.com").estado(EstadoPasaje.RESERVADO).build();
        Pasaje p2 = Pasaje.builder().id(2L).usuarioEmail("user@mail.com").estado(EstadoPasaje.PAGADO).build();
        when(pasajeRepository.findByUsuarioEmailAndEstadoIn(eq("user@mail.com"),
                eq(List.of(EstadoPasaje.RESERVADO, EstadoPasaje.PAGADO, EstadoPasaje.FINALIZADO))))
                .thenReturn(List.of(p1, p2));

        List<Pasaje> resultados = pasajeService.misPasajes("user@mail.com");

        assertEquals(2, resultados.size());
        verify(pasajeRepository, times(1))
                .findByUsuarioEmailAndEstadoIn(anyString(), anyList());
    }

    @Test
    void obtenerPasajesDTO_debeRetornarDTOsDelUsuario() {
        Pasaje p1 = Pasaje.builder().id(1L).usuarioEmail("user@mail.com").nombrePasajero("A")
                .estado(EstadoPasaje.RESERVADO).build();
        when(pasajeRepository.findByUsuarioEmailAndEstadoIn(eq("user@mail.com"), anyList()))
                .thenReturn(List.of(p1));

        List<PasajeDTO> resultados = pasajeService.obtenerPasajesDTO("user@mail.com");

        assertEquals(1, resultados.size());
        assertEquals("A", resultados.get(0).getNombrePasajero());
    }

    // ========================================================================
    // Business logic: obtenerPorCodigoBoleto
    // ========================================================================

    @Test
    void obtenerPorCodigoBoleto_conCodigoValido_debeRetornarPasaje() {
        when(pasajeRepository.findByCodigoBoleto("B-ABC123"))
                .thenReturn(Optional.of(pasaje));

        Optional<Pasaje> resultado = pasajeService.obtenerPorCodigoBoleto("B-ABC123");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez", resultado.get().getNombrePasajero());
    }

    @Test
    void obtenerPorCodigoBoleto_conCodigoInvalido_debeRetornarVacio() {
        when(pasajeRepository.findByCodigoBoleto("INVALIDO"))
                .thenReturn(Optional.empty());

        Optional<Pasaje> resultado = pasajeService.obtenerPorCodigoBoleto("INVALIDO");

        assertFalse(resultado.isPresent());
    }

    @Test
    void obtenerPasajeDTOPorCodigo_conCodigoValido_debeRetornarDTO() {
        when(pasajeRepository.findByCodigoBoleto("B-VALIDO"))
                .thenReturn(Optional.of(pasaje));

        Optional<PasajeDTO> resultado = pasajeService.obtenerPasajeDTOPorCodigo("B-VALIDO");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez", resultado.get().getNombrePasajero());
    }
}
