package proyecto.nuevaases.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.enums.EstadoVehiculo;
import proyecto.nuevaases.models.enums.Rol;
import proyecto.nuevaases.models.enums.TipoVehiculo;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        limpiarEnumsInvalidos();

        // ==================== USUARIOS ====================
        if (!usuarioRepository.existsByEmail("admin@empresa.com")) {
            Usuario admin = Usuario.builder()
                    .email("admin@empresa.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombreCompleto("Administrador del Sistema")
                    .dni("00000001")
                    .telefono("999888001")
                    .rol(Rol.ADMINISTRADOR)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("✅ Admin creado: admin@empresa.com / admin123");
        }

        if (!usuarioRepository.existsByEmail("cliente@test.com")) {
            Usuario cliente = Usuario.builder()
                    .email("cliente@test.com")
                    .password(passwordEncoder.encode("cliente123"))
                    .nombreCompleto("Cliente de Prueba")
                    .dni("12345678")
                    .telefono("999888002")
                    .rol(Rol.CLIENTE)
                    .activo(true)
                    .build();
            usuarioRepository.save(cliente);
            log.info("✅ Cliente creado: cliente@test.com / cliente123");
        }

        if (!usuarioRepository.existsByEmail("conductor@test.com")) {
            Usuario conductor = Usuario.builder()
                    .email("conductor@test.com")
                    .password(passwordEncoder.encode("conductor123"))
                    .nombreCompleto("Conductor de Prueba")
                    .dni("87654321")
                    .telefono("999888003")
                    .rol(Rol.CONDUCTOR)
                    .activo(true)
                    .build();
            usuarioRepository.save(conductor);
            log.info("✅ Conductor creado: conductor@test.com / conductor123");
        }

        // ==================== VEHÍCULOS ====================
        if (vehiculoRepository.count() == 0) {
            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-001")
                    .marca("Mercedes-Benz")
                    .modelo("Sprinter")
                    .anio(2023)
                    .capacidad(20)
                    .tipo(TipoVehiculo.BUS)
                    .estado(EstadoVehiculo.DISPONIBLE)
                    .precioPorDia(350.0)
                    .imagen("/img/minivanblanca.png")
                    .build());

            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-002")
                    .marca("Toyota")
                    .modelo("Hiace")
                    .anio(2022)
                    .capacidad(12)
                    .tipo(TipoVehiculo.MINIVAN)
                    .estado(EstadoVehiculo.DISPONIBLE)
                    .precioPorDia(220.0)
                    .imagen("/img/minivanploma.png")
                    .build());

            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-003")
                    .marca("Nissan")
                    .modelo("Frontier")
                    .anio(2023)
                    .capacidad(5)
                    .tipo(TipoVehiculo.CAMIONETA)
                    .estado(EstadoVehiculo.DISPONIBLE)
                    .precioPorDia(180.0)
                    .imagen("")
                    .build());

            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-004")
                    .marca("Volkswagen")
                    .modelo("Crafter")
                    .anio(2024)
                    .capacidad(30)
                    .tipo(TipoVehiculo.BUS)
                    .estado(EstadoVehiculo.DISPONIBLE)
                    .precioPorDia(450.0)
                    .imagen("/img/minivanploma2.png")
                    .build());

            log.info("✅ 4 vehículos de ejemplo creados");
        }

        // ==================== VIAJES ====================
        // Los viajes ahora se gestionan desde la página web.
        // El administrador puede crear viajes desde:
        //   - /viajes/nuevo (individual)
        //   - /viajes/generar-masivo (generación masiva)
        log.info("ℹ️ Viajes: la gestión se realiza desde la interfaz web (/viajes)");
    }

    private void limpiarEnumsInvalidos() {
        int viajes = jdbcTemplate.update("UPDATE viajes SET estado_viaje = 'PROGRAMADO' WHERE estado_viaje IS NULL OR estado_viaje = '' OR estado_viaje NOT IN ('PROGRAMADO','EN_CURSO','FINALIZADO')");
        int pasajes = jdbcTemplate.update("UPDATE pasajes SET estado = 'RESERVADO' WHERE estado IS NULL OR estado = '' OR estado NOT IN ('RESERVADO','PAGADO','CANCELADO','FINALIZADO')");
        int encomiendas = jdbcTemplate.update("UPDATE encomiendas SET estado = 'REGISTRADO' WHERE estado IS NULL OR estado = '' OR estado NOT IN ('REGISTRADO','EN_TRANSITO','EN_DESTINO','ENTREGADO')");
        int usuarios = jdbcTemplate.update("UPDATE usuarios SET estado_postulacion = 'PENDIENTE' WHERE estado_postulacion IS NULL OR estado_postulacion = '' OR estado_postulacion NOT IN ('PENDIENTE','APROBADO','RECHAZADO')");
        if (viajes > 0 || pasajes > 0 || encomiendas > 0 || usuarios > 0) {
            log.warn("🧹 Limpieza de enums inválidos: {} viajes, {} pasajes, {} encomiendas, {} usuarios reparados", viajes, pasajes, encomiendas, usuarios);
        }
    }
}
