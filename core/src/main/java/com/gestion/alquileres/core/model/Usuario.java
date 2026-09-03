package com.gestion.alquileres.core.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cuenta de acceso a la aplicacion.
 *
 * <p>El campo {@code email} es el buzon al que se envia el aviso cuando la
 * contrasena caduca. Las cuentas con {@code passwordNuncaCaduca = true}
 * (por ejemplo {@code system}) nunca se consideran caducadas.</p>
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "rol", nullable = false, length = 30)
    private String rol = "ROLE_USER";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "password_nunca_caduca", nullable = false)
    private boolean passwordNuncaCaduca = false;

    @Column(name = "fecha_caducidad_password")
    private LocalDate fechaCaducidadPassword;

    @Column(name = "fecha_ultimo_aviso")
    private LocalDateTime fechaUltimoAviso;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta = LocalDateTime.now();

    /** @return true si la contrasena esta caducada a dia de hoy. */
    @Transient
    public boolean isPasswordCaducada() {
        return !passwordNuncaCaduca
                && fechaCaducidadPassword != null
                && fechaCaducidadPassword.isBefore(LocalDate.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public boolean isPasswordNuncaCaduca() { return passwordNuncaCaduca; }
    public void setPasswordNuncaCaduca(boolean v) { this.passwordNuncaCaduca = v; }
    public LocalDate getFechaCaducidadPassword() { return fechaCaducidadPassword; }
    public void setFechaCaducidadPassword(LocalDate f) { this.fechaCaducidadPassword = f; }
    public LocalDateTime getFechaUltimoAviso() { return fechaUltimoAviso; }
    public void setFechaUltimoAviso(LocalDateTime f) { this.fechaUltimoAviso = f; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime f) { this.fechaAlta = f; }
}
