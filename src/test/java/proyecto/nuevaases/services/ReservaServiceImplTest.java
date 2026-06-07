package proyecto.nuevaases.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.nuevaases.dto.PasajeroReservaDTO;
import proyecto.nuevaases.models.Reserva;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.ReservaRepository;
import proyecto.nuevaases.repositories.ViajeRepository;
import proyecto.nuevaases.services.impl.ReservaServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ViajeRepository viajeRepository;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Viaje viaje;

    @BeforeEach
    void setUp() {
        viaje = Viaje.builder()
                .id(1L)
                .origen("Buenos Aires")
                .destino("Córdoba")
                .fecha(LocalDate.now().plusDays(1))
                .horaSalida("10:30")
                .precio(500.0)
                .totalAsientos(50)
                .build();
    }

    @Test
    void asientosOcupados_debeRetornarListaDeAsientosReservados() {
        // Arrange
        Reserva r1 = Reserva.builder().asiento(1).estado("RESERVADO").build();
        Reserva r2 = Reserva.builder().asiento(5).estado("PAGADO").build();
        Reserva r3 = Reserva.builder().asiento(10).estado("CANCELADO").build();

        when(reservaRepository.findByViajeAndEstadoIn(viaje, List.of("RESERVADO", "PAGADO")))
                .thenReturn(List.of(r1, r2));

        // Act
        List<Integer> ocupados = reservaService.asientosOcupados(viaje);

        // Assert
        assertEquals(2, ocupados.size());
        assertTrue(ocupados.contains(1));
        assertTrue(ocupados.contains(5));
        assertFalse(ocupados.contains(10));
    }

    @Test
    void reservar_conAsientoDisponible_debeCrearReserva() {
        // Arrange
        String usuarioEmail = "usuario@example.com";
        String nombrePasajero = "Juan Pérez";
        String dniPasajero = "12345678";
        int asiento = 5;

        Reserva reservaEsperada = Reserva.builder()
                .usuarioEmail(usuarioEmail)
                .viaje(viaje)
                .asiento(asiento)
                .nombrePasajero(nombrePasajero)
                .dniPasajero(dniPasajero)
                .estado("RESERVADO")
                .precio(500.0)
                .build();

        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaEsperada);

        // Act
        Reserva resultado = reservaService.reservar(usuarioEmail, viaje, asiento, nombrePasajero, dniPasajero);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuarioEmail, resultado.getUsuarioEmail());
        assertEquals(asiento, resultado.getAsiento());
        assertEquals("RESERVADO", resultado.getEstado());
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void reservarMultiples_conPasajerosValidos_debeCrearTodasLasReservas() {
        // Arrange
        String usuarioEmail = "usuario@example.com";
        List<PasajeroReservaDTO> pasajeros = List.of(
                new PasajeroReservaDTO("Juan", "12345678", 1),
                new PasajeroReservaDTO("María", "87654321", 2));

        when(reservaRepository.findByViajeAndEstadoIn(viaje, List.of("RESERVADO", "PAGADO")))
                .thenReturn(List.of());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Reserva> resultado = reservaService.reservarMultiples(usuarioEmail, viaje, pasajeros);

        // Assert
        assertEquals(2, resultado.size());
        verify(reservaRepository, times(2)).save(any(Reserva.class));
    }

    @Test
    void reservarMultiples_conAsientosDuplicados_debeThrowException() {
        // Arrange
        String usuarioEmail = "usuario@example.com";
        List<PasajeroReservaDTO> pasajeros = List.of(
                new PasajeroReservaDTO("Juan", "12345678", 1),
                new PasajeroReservaDTO("María", "87654321", 1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reservaService.reservarMultiples(usuarioEmail, viaje, pasajeros));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void reservarMultiples_conPasajerosVacio_debeThrowException() {
        // Arrange
        String usuarioEmail = "usuario@example.com";
        List<PasajeroReservaDTO> pasajeros = List.of();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reservaService.reservarMultiples(usuarioEmail, viaje, pasajeros));
    }

    @Test
    void reservarMultiples_conAsientoOcupado_debeThrowException() {
        // Arrange
        String usuarioEmail = "usuario@example.com";
        List<PasajeroReservaDTO> pasajeros = List.of(
                new PasajeroReservaDTO("Juan", "12345678", 1),
                new PasajeroReservaDTO("María", "87654321", 5));

        Reserva reservaOcupada = Reserva.builder().asiento(5).estado("PAGADO").build();
        when(reservaRepository.findByViajeAndEstadoIn(viaje, List.of("RESERVADO", "PAGADO")))
                .thenReturn(List.of(reservaOcupada));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reservaService.reservarMultiples(usuarioEmail, viaje, pasajeros));
    }

    @Test
    void asientosOcupados_sinReservas_debeRetornarListaVacia() {
        // Arrange
        when(reservaRepository.findByViajeAndEstadoIn(viaje, List.of("RESERVADO", "PAGADO")))
                .thenReturn(List.of());

        // Act
        List<Integer> ocupados = reservaService.asientosOcupados(viaje);

        // Assert
        assertTrue(ocupados.isEmpty());
    }
}
