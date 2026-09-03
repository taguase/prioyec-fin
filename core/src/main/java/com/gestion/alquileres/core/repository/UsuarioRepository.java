package com.gestion.alquileres.core.repository;

import com.gestion.alquileres.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    /** Cuentas activas, con caducidad definida, ya vencidas y que no son "nunca caduca". */
    @Query("""
           select u from Usuario u
            where u.activo = true
              and u.passwordNuncaCaduca = false
              and u.fechaCaducidadPassword is not null
              and u.fechaCaducidadPassword < :hoy
           """)
    List<Usuario> findConPasswordCaducada(@Param("hoy") LocalDate hoy);

    /** Cuentas activas cuya contrasena caduca dentro de la ventana de preaviso. */
    List<Usuario> findByActivoTrueAndPasswordNuncaCaducaFalseAndFechaCaducidadPasswordBetween(
            LocalDate desde, LocalDate hasta);
}
