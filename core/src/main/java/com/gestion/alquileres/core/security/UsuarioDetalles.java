package com.gestion.alquileres.core.security;

import com.gestion.alquileres.core.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Adaptador entre la entidad {@link Usuario} y Spring Security. */
public class UsuarioDetalles implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetalles(Usuario usuario) { this.usuario = usuario; }

    public Usuario getUsuario() { return usuario; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(usuario.getRol()));
    }

    @Override public String getPassword() { return usuario.getPassword(); }
    @Override public String getUsername() { return usuario.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }

    /** Aqui es donde se aplica la caducidad: 'system' nunca caduca. */
    @Override public boolean isCredentialsNonExpired() { return !usuario.isPasswordCaducada(); }

    @Override public boolean isEnabled() { return usuario.isActivo(); }
}
