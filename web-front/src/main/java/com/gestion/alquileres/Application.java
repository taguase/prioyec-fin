package com.gestion.alquileres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion.
 *
 * <p>Vive en el paquete raiz {@code com.gestion.alquileres} a proposito: asi el
 * escaneo de componentes, entidades y repositorios alcanza a los tres modulos
 * (core, excel y web-front) sin configuracion adicional.</p>
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
