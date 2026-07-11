package proyecto.nuevaases.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import proyecto.nuevaases.models.enums.EstadoPostulacion;
import proyecto.nuevaases.models.enums.Rol;
import proyecto.nuevaases.repositories.UsuarioRepository;

// Marca esta clase como fuente de configuración de Spring
@Configuration
// Habilita la seguridad web de Spring Security (reemplaza WebSecurityConfigurerAdapter)
@EnableWebSecurity
public class SecurityConfig {

        // Contraseña del admin hardcodeada encriptada con BCrypt (admin123)
        private static final String ADMIN_PASSWORD_ENCODED = "$2a$10$BM5/zHCcvhmuH7RgDCMhw.uJMn8rr8.cNzYzV8qrGA7jk9uawMNFm";

        // Define la cadena de filtros de seguridad (la configuración principal)
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                // Configuración especial para CSRF: no requiere que el token esté en un atributo de la request
                CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName(null);

                http
                    // --- CONFIGURACIÓN CSRF ---
                    // Guarda el token CSRF en una cookie (no HttpOnly para que JS lo lea)
                    // Desactiva CSRF para la consola H2 y los endpoints de la API REST
                    .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/h2-console/**", "/api/**"))
                    // --- HEADERS DE SEGURIDAD ---
                    // Permite que la consola H2 se muestre en iframes del mismo origen
                    .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                    // --- AUTORIZACIÓN DE SOLICITUDES ---
                    // Define qué rutas son públicas y cuáles requieren autenticación
                    .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: páginas principales, recursos estáticos, alquiler
                        .requestMatchers(
                            "/", "/index.html", "/inicio", "/nosotros",
                            "/contacto", "/registro", "/postular-conductor",
                            "/postulacion-enviada",
                            "/css/**", "/js/**",
                            "/images/**", "/img/**", "/node_modules/**",
                            "/webjars/**", "/alquiler/**")
                            .permitAll()
                        // La consola H2 también es pública
                        .requestMatchers("/h2-console/**").permitAll()
                        // Cualquier otra ruta requiere estar autenticado
                        .anyRequest().authenticated())
                    // --- FORMULARIO DE LOGIN ---
                    // Página de login personalizada, redirige a /dashboard al iniciar sesión
                    .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                    // --- CONFIGURACIÓN DE LOGOUT ---
                    // Cierra sesión en /logout, invalida la sesión y borra la cookie JSESSIONID
                    .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

                return http.build();
        }

        // Servicio personalizado que carga un usuario desde la BD (o admin hardcodeado)
        @Bean
        public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
                // Retorna una función lambda que recibe el email (username) y busca al usuario
                return username -> {
                        // 1. Buscar al usuario por email en la base de datos
                        var usuarioOpt = usuarioRepository.findByEmail(username);
                        if (usuarioOpt.isPresent()) {
                                var usuario = usuarioOpt.get();
                                // 2. Si el usuario existe pero está desactivado, denegar acceso
                                if (!usuario.isActivo()) {
                                        throw new UsernameNotFoundException("Usuario inactivo");
                                }
                                // 3. Si es conductor y su postulación no fue aprobada, denegar acceso
                                if (usuario.getRol() == Rol.CONDUCTOR && usuario.getEstadoPostulacion() != EstadoPostulacion.APROBADO) {
                                        String estado = usuario.getEstadoPostulacion() != null ? usuario.getEstadoPostulacion().name().toLowerCase() : "pendiente";
                                        throw new UsernameNotFoundException("Conductor no aprobado. Tu postulación está " + estado);
                                }
                                // 4. Construye el objeto UserDetails de Spring con los datos del usuario
                                return org.springframework.security.core.userdetails.User.builder()
                                                .username(usuario.getEmail())
                                                .password(usuario.getPassword())
                                                .roles(usuario.getRol().name())
                                                .build();
                        }

                        // 5. Si no se encontró en BD, verificar si es el admin hardcodeado
                        if ("admin@empresa.com".equals(username)) {
                                return org.springframework.security.core.userdetails.User.builder()
                                                .username("admin@empresa.com")
                                                .password(ADMIN_PASSWORD_ENCODED)
                                                .roles("ADMINISTRADOR")
                                                .build();
                        }

                        // 6. Si no se encontró en ningún lado, lanzar excepción
                        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
                };
        }

        // Bean que Spring Security usa para encriptar/verificar contraseñas con BCrypt
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
