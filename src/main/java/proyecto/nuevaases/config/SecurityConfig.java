package proyecto.nuevaases.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import proyecto.nuevaases.repositories.UsuarioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .ignoringRequestMatchers("/h2-console/**", "/api/**"))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/index.html", "/inicio", "/nosotros",
                                                                "/registro", "/css/**", "/js/**",
                                                                "/images/**", "/img/**", "/node_modules/**",
                                                                "/webjars/**")
                                                .permitAll()
                                                .requestMatchers("/api/**").permitAll() // ← AGREGAR ESTA LÍNEA
                                                .requestMatchers(HttpMethod.GET, "/vehiculos/**", "/pasajes/**",
                                                                "/encomiendas/**", "/rastrear")
                                                .permitAll()
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/encomiendas/nuevo", "/encomiendas/editar/**",
                                                                "/encomiendas/eliminar/**",
                                                                "/pasajes/nuevo", "/pasajes/editar/**",
                                                                "/pasajes/eliminar/**",
                                                                "/vehiculos/nuevo", "/vehiculos/editar/**",
                                                                "/vehiculos/eliminar/**")
                                                .hasAnyRole("ADMINISTRADOR", "CLIENTE", "CONDUCTOR")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder) {
                return username -> {
                        var usuarioOpt = usuarioRepository.findByEmail(username);
                        if (usuarioOpt.isPresent()) {
                                var usuario = usuarioOpt.get();
                                if (!usuario.isActivo()) {
                                        throw new UsernameNotFoundException("Usuario inactivo");
                                }
                                return org.springframework.security.core.userdetails.User.builder()
                                                .username(usuario.getEmail())
                                                .password(usuario.getPassword())
                                                .roles(usuario.getRol())
                                                .build();
                        }

                        if ("admin@empresa.com".equals(username)) {
                                return org.springframework.security.core.userdetails.User.builder()
                                                .username("admin@empresa.com")
                                                .password(passwordEncoder.encode("admin123"))
                                                .roles("ADMINISTRADOR")
                                                .build();
                        }

                        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
                };
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
