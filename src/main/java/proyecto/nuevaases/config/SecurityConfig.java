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
import proyecto.nuevaases.repositories.UsuarioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final String ADMIN_PASSWORD_ENCODED = "$2a$10$BM5/zHCcvhmuH7RgDCMhw.uJMn8rr8.cNzYzV8qrGA7jk9uawMNFm";

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

               // En securityFilterChain:
CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
requestHandler.setCsrfRequestAttributeName(null); // ✅ clave: null fuerza resolución inmediata

http
    .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(requestHandler)
        .ignoringRequestMatchers("/h2-console/**", "/api/**"))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/", "/index.html", "/inicio", "/nosotros",
                                                                "/registro", "/css/**", "/js/**",
                                                                "/images/**", "/img/**", "/node_modules/**",
                                                                "/webjars/**",  "/alquiler/**")
                                                .permitAll()
                                                .requestMatchers("/h2-console/**").permitAll()
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
        public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
                return username -> {
                        var usuarioOpt = usuarioRepository.findByEmail(username);
                        if (usuarioOpt.isPresent()) {
                                var usuario = usuarioOpt.get();
                                if (!usuario.isActivo()) {
                                        throw new UsernameNotFoundException("Usuario inactivo");
                                }
                                // Bloquear conductores no aprobados
                                if ("CONDUCTOR".equals(usuario.getRol()) && !"APROBADO".equals(usuario.getEstadoPostulacion())) {
                                        throw new UsernameNotFoundException("Conductor no aprobado. Tu postulación está " + usuario.getEstadoPostulacion().toLowerCase());
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
                                                .password(ADMIN_PASSWORD_ENCODED)
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