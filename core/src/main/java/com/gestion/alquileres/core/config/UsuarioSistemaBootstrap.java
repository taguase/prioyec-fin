package com.gestion.alquileres.core.config;

import com.gestion.alquileres.core.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Asegura en cada arranque que la cuenta tecnica {@code system} existe, esta
 * activa y tiene la contrasena marcada como "no caduca nunca". Complementa a la
 * migracion V2, que la crea la primera vez.
 */
@Component
public class UsuarioSistemaBootstrap implements ApplicationRunner {

    private final UsuarioService usuarioService;
    private final String password;
    private final String email;

    public UsuarioSistemaBootstrap(UsuarioService usuarioService,
                                   @Value("${app.usuario-sistema.password:system}") String password,
                                   @Value("${app.usuario-sistema.email:system@gestion-alquileres.local}") String email) {
        this.usuarioService = usuarioService;
        this.password = password;
        this.email = email;
    }

    @Override
    public void run(ApplicationArguments args) {
        usuarioService.garantizarUsuarioSistema(password, email);
    }
}
