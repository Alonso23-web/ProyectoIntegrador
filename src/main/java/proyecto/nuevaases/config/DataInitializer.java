package proyecto.nuevaases.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import proyecto.nuevaases.models.Usuario;
import proyecto.nuevaases.models.Vehiculo;
import proyecto.nuevaases.models.Viaje;
import proyecto.nuevaases.repositories.UsuarioRepository;
import proyecto.nuevaases.repositories.VehiculoRepository;
import proyecto.nuevaases.repositories.ViajeRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ViajeRepository viajeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // ==================== USUARIOS ====================
        if (!usuarioRepository.existsByEmail("admin@empresa.com")) {
            Usuario admin = Usuario.builder()
                    .email("admin@empresa.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombreCompleto("Administrador del Sistema")
                    .dni("00000001")
                    .telefono("999888001")
                    .rol("ADMINISTRADOR")
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
                    .rol("CLIENTE")
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
                    .rol("CONDUCTOR")
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
                    .tipo("BUS")
                    .estado("DISPONIBLE")
                    .precioPorDia(350.0)
                    .imagen("/img/minivanblanca.png")
                    .build());

            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-002")
                    .marca("Toyota")
                    .modelo("Hiace")
                    .anio(2022)
                    .capacidad(12)
                    .tipo("MINIVAN")
                    .estado("DISPONIBLE")
                    .precioPorDia(220.0)
                    .imagen("/img/minivanploma.png")
                    .build());

            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-003")
                    .marca("Nissan")
                    .modelo("Frontier")
                    .anio(2023)
                    .capacidad(5)
                    .tipo("CAMIONETA")
                    .estado("DISPONIBLE")
                    .precioPorDia(180.0)
                    .imagen("")
                    .build());

            vehiculoRepository.save(Vehiculo.builder()
                    .placa("ABC-004")
                    .marca("Volkswagen")
                    .modelo("Crafter")
                    .anio(2024)
                    .capacidad(30)
                    .tipo("BUS")
                    .estado("DISPONIBLE")
                    .precioPorDia(450.0)
                    .imagen("/img/minivanploma2.png")
                    .build());

            log.info("✅ 4 vehículos de ejemplo creados");
        }

        // ==================== VIAJES ====================
        if (viajeRepository.count() == 0) {
            LocalDate hoy = LocalDate.now();
            String[] horas = {"08:00", "12:00", "16:00", "20:00"};
            double precioBase = 25.0;

            // Crear viajes para los próximos 7 días
            for (int dia = 0; dia < 7; dia++) {
                LocalDate fecha = hoy.plusDays(dia);
                for (String hora : horas) {
                    viajeRepository.save(Viaje.builder()
                            .origen("Trujillo")
                            .destino("Chepén")
                            .fecha(fecha)
                            .horaSalida(hora)
                            .tipoBus("BUS")
                            .totalAsientos(40)
                            .precio(precioBase)
                            .creadoPorEmail("admin@empresa.com")
                            .build());

                    viajeRepository.save(Viaje.builder()
                            .origen("Chepén")
                            .destino("Trujillo")
                            .fecha(fecha)
                            .horaSalida(hora)
                            .tipoBus("MINIVAN")
                            .totalAsientos(20)
                            .precio(precioBase)
                            .creadoPorEmail("admin@empresa.com")
                            .build());
                }
            }

            long totalViajes = viajeRepository.count();
            log.info("✅ {} viajes de ejemplo creados (Trujillo ↔ Chepén, 7 días)", totalViajes);
        }
    }
}
