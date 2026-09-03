package com.gestion.alquileres.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuracion del modulo core: propiedades funcionales, cifrado de
 * contrasenas y activacion del planificador de tareas.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
@EnableTransactionManagement
@EnableScheduling
public class CoreConfig {

    /** BCrypt fuerza 10, compatible con los hashes sembrados en la migracion V2. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
