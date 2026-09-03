package com.gestion.alquileres.web.config;

import com.gestion.alquileres.core.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * Login por formulario contra la tabla {@code usuario}.
 *
 * <p>Cuando el fallo de autenticacion es por contrasena caducada se dispara el
 * envio del correo de aviso al buzon grabado del usuario y se le redirige a
 * {@code /login?caducada}. La cuenta {@code system} no puede caducar, de modo
 * que nunca pasa por esta rama.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final UsuarioService usuarioService;

    public SecurityConfig(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/inicio", true)
                .failureHandler(manejadorDeFallo())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /** Distingue la caducidad de contrasena del resto de errores de login. */
    @Bean
    public AuthenticationFailureHandler manejadorDeFallo() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            SimpleUrlAuthenticationFailureHandler delegado;
            if (exception instanceof CredentialsExpiredException) {
                boolean enviado = usuarioService.avisarCaducidad(username);
                log.info("Login rechazado por contrasena caducada (usuario={}, aviso enviado={})", username, enviado);
                delegado = new SimpleUrlAuthenticationFailureHandler("/login?caducada");
            } else {
                delegado = new SimpleUrlAuthenticationFailureHandler("/login?error");
            }
            delegado.onAuthenticationFailure(request, response, exception);
        };
    }
}
