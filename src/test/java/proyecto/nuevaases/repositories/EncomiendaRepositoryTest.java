package proyecto.nuevaases.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import proyecto.nuevaases.models.Encomienda;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.models.enums.EstadoEncomienda;
import proyecto.nuevaases.models.enums.EstadoViaje;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=update"
})
class EncomiendaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EncomiendaRepository encomiendaRepository;

    private Viaje viajeConductorA;
    private Viaje viajeConductorB;

    @BeforeEach
    void setUp() {
        // Viaje del Conductor A
        viajeConductorA = Viaje.builder()
                .origen("Trujillo")
                .destino("Chepén")
                .fecha(LocalDate.now())
                .horaSalida("08:00")
                .tipoBus("MINIVAN")
                .totalAsientos(15)
                .precio(12.0)
                .creadoPorEmail("admin@empresa.com")
                .conductorEmail("conductorA@test.com")
                .estadoViaje(EstadoViaje.FINALIZADO)
                .build();
        entityManager.persist(viajeConductorA);

        // Viaje del Conductor B
        viajeConductorB = Viaje.builder()
                .origen("Trujillo")
                .destino("Pacasmayo")
                .fecha(LocalDate.now())
                .horaSalida("10:00")
                .tipoBus("MINIVAN")
                .totalAsientos(15)
                .precio(10.0)
                .creadoPorEmail("admin@empresa.com")
                .conductorEmail("conductorB@test.com")
                .estadoViaje(EstadoViaje.FINALIZADO)
                .build();
        entityManager.persist(viajeConductorB);

        // Encomienda 1: Conductor A, ENTREGADO → debe contar
        Encomienda enc1 = Encomienda.builder()
                .codigoRastreo("NAE-TEST-001")
                .remitente("Cliente1")
                .dniRemitente("11111111")
                .destinatario("Dest1")
                .dniDestinatario("22222222")
                .origen("Trujillo")
                .destino("Chepén")
                .descripcion("Caja")
                .peso(5.0)
                .precio(10.0)
                .fechaEnvio(LocalDate.now())
                .estado(EstadoEncomienda.ENTREGADO)
                .observaciones("-")
                .creadoPorEmail("admin@empresa.com")
                .viajeAsignado(viajeConductorA)
                .build();
        entityManager.persist(enc1);

        // Encomienda 2: Conductor A, REGISTRADO → NO debe contar (estado incorrecto)
        Encomienda enc2 = Encomienda.builder()
                .codigoRastreo("NAE-TEST-002")
                .remitente("Cliente2")
                .dniRemitente("33333333")
                .destinatario("Dest2")
                .dniDestinatario("44444444")
                .origen("Trujillo")
                .destino("Chepén")
                .descripcion("Documentos")
                .peso(2.0)
                .precio(8.0)
                .fechaEnvio(LocalDate.now())
                .estado(EstadoEncomienda.REGISTRADO)
                .observaciones("-")
                .creadoPorEmail("admin@empresa.com")
                .viajeAsignado(viajeConductorA)
                .build();
        entityManager.persist(enc2);

        // Encomienda 3: Conductor B, ENTREGADO → debe contar para conductor B
        Encomienda enc3 = Encomienda.builder()
                .codigoRastreo("NAE-TEST-003")
                .remitente("Cliente3")
                .dniRemitente("55555555")
                .destinatario("Dest3")
                .dniDestinatario("66666666")
                .origen("Pacasmayo")
                .destino("Trujillo")
                .descripcion("Ropa")
                .peso(3.0)
                .precio(9.0)
                .fechaEnvio(LocalDate.now())
                .estado(EstadoEncomienda.ENTREGADO)
                .observaciones("-")
                .creadoPorEmail("admin@empresa.com")
                .viajeAsignado(viajeConductorB)
                .build();
        entityManager.persist(enc3);

        // Encomienda 4: Sin viaje asignado, ENTREGADO → NO debe contar para nadie
        Encomienda enc4 = Encomienda.builder()
                .codigoRastreo("NAE-TEST-004")
                .remitente("Cliente4")
                .dniRemitente("77777777")
                .destinatario("Dest4")
                .dniDestinatario("88888888")
                .origen("Chepén")
                .destino("Trujillo")
                .descripcion("Electrónicos")
                .peso(1.0)
                .precio(7.0)
                .fechaEnvio(LocalDate.now())
                .estado(EstadoEncomienda.ENTREGADO)
                .observaciones("-")
                .creadoPorEmail("admin@empresa.com")
                .build();
        entityManager.persist(enc4);
    }

    @Test
    void countByViajeConductorEmailAndEstado_debeContarSoloLasEntregadasDelConductor() {
        long resultadoA = encomiendaRepository.countByViajeConductorEmailAndEstado(
                "conductorA@test.com", EstadoEncomienda.ENTREGADO);
        assertEquals(1, resultadoA, "Conductor A debería tener 1 encomienda entregada");
    }

    @Test
    void countByViajeConductorEmailAndEstado_conEstadoRegistrado_debeRetornarUno() {
        long resultadoA = encomiendaRepository.countByViajeConductorEmailAndEstado(
                "conductorA@test.com", EstadoEncomienda.REGISTRADO);
        assertEquals(1, resultadoA, "Conductor A debería tener 1 encomienda en estado REGISTRADO");
    }

    @Test
    void countByViajeConductorEmailAndEstado_conOtroConductor_debeContarCorrectamente() {
        long resultadoB = encomiendaRepository.countByViajeConductorEmailAndEstado(
                "conductorB@test.com", EstadoEncomienda.ENTREGADO);
        assertEquals(1, resultadoB, "Conductor B debería tener 1 encomienda entregada");
    }

    @Test
    void countByViajeConductorEmailAndEstado_sinEncomiendas_debeRetornarCero() {
        long resultado = encomiendaRepository.countByViajeConductorEmailAndEstado(
                "conductorInexistente@test.com", EstadoEncomienda.ENTREGADO);
        assertEquals(0, resultado, "Conductor inexistente debería tener 0 encomiendas");
    }

    @Test
    void countByViajeConductorEmailAndEstado_encomiendaSinViajeNoDebeContar() {
        long resultado = encomiendaRepository.countByViajeConductorEmailAndEstado(
                "conductorA@test.com", EstadoEncomienda.ENTREGADO);
        // La encomienda #4 no tiene viaje asignado, no debe contar
        assertEquals(1, resultado);
    }
}
