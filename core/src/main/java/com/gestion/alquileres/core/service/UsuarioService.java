package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.config.AppProperties;
import com.gestion.alquileres.core.model.Usuario;
import com.gestion.alquileres.core.repository.UsuarioRepository;
import com.gestion.alquileres.core.security.UsuarioDetalles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Alta, consulta y mantenimiento de las cuentas de acceso. */
@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    /** Cuenta tecnica: siempre existe y su contrasena no caduca nunca. */
    public static final String USUARIO_SISTEMA = "system";

    private final UsuarioRepository repositorio;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final AppProperties propiedades;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder encoder,
                          EmailService emailService, AppProperties propiedades) {
        this.repositorio = repositorio;
        this.encoder = encoder;
        this.emailService = emailService;
        this.propiedades = propiedades;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return new UsuarioDetalles(u);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return repositorio.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() { return repositorio.findAll(); }

    /**
     * Garantiza que el usuario {@code system} existe con contrasena {@code system}
     * y marcado como "nunca caduca". Se ejecuta en cada arranque.
     */
    @Transactional
    public Usuario garantizarUsuarioSistema(String passwordPorDefecto, String email) {
        Usuario u = repositorio.findByUsername(USUARIO_SISTEMA).orElseGet(Usuario::new);
        boolean nuevo = u.getId() == null;
        if (nuevo) {
            u.setUsername(USUARIO_SISTEMA);
            u.setPassword(encoder.encode(passwordPorDefecto));
            u.setNombreCompleto("Usuario tecnico del sistema");
            u.setRol("ROLE_ADMIN");
        }
        if (u.getEmail() == null || u.getEmail().isBlank()) u.setEmail(email);
        // Invariantes de la cuenta tecnica: siempre activa y sin caducidad.
        u.setActivo(true);
        u.setPasswordNuncaCaduca(true);
        u.setFechaCaducidadPassword(null);
        Usuario guardado = repositorio.save(u);
        log.info("Usuario '{}' {} (password nunca caduca)", USUARIO_SISTEMA, nuevo ? "creado" : "verificado");
        return guardado;
    }

    /** Alta de una cuenta normal, con la caducidad calculada segun configuracion. */
    @Transactional
    public Usuario crear(String username, String passwordEnClaro, String email, String nombreCompleto, String rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(passwordEnClaro));
        u.setEmail(email);
        u.setNombreCompleto(nombreCompleto);
        u.setRol(rol == null || rol.isBlank() ? "ROLE_USER" : rol);
        u.setActivo(true);
        u.setPasswordNuncaCaduca(false);
        u.setFechaCaducidadPassword(LocalDate.now().plusDays(propiedades.getPassword().getDiasValidez()));
        return repositorio.save(u);
    }

    /** Cambio de contrasena: reinicia el contador de caducidad. */
    @Transactional
    public void cambiarPassword(String username, String passwordNuevaEnClaro) {
        Usuario u = repositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        u.setPassword(encoder.encode(passwordNuevaEnClaro));
        if (!u.isPasswordNuncaCaduca()) {
            u.setFechaCaducidadPassword(LocalDate.now().plusDays(propiedades.getPassword().getDiasValidez()));
        }
        u.setFechaUltimoAviso(null);
        repositorio.save(u);
    }

    /**
     * Envia el aviso de contrasena caducada al correo grabado del usuario.
     * Se invoca cuando el login falla por credenciales caducadas, y como maximo
     * una vez al dia por usuario.
     *
     * @return true si se ha enviado un aviso en esta llamada
     */
    @Transactional
    public boolean avisarCaducidad(String username) {
        Optional<Usuario> opt = repositorio.findByUsername(username);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        if (!u.isPasswordCaducada()) return false;
        if (u.getFechaUltimoAviso() != null
                && u.getFechaUltimoAviso().toLocalDate().isEqual(LocalDate.now())) {
            return false; // ya avisado hoy
        }
        boolean enviado = emailService.enviarAvisoCaducada(u);
        u.setFechaUltimoAviso(LocalDateTime.now());
        repositorio.save(u);
        return enviado;
    }

    /** Cuentas activas cuya contrasena ya ha vencido. */
    @Transactional(readOnly = true)
    public List<Usuario> listarCaducadas() {
        return repositorio.findConPasswordCaducada(LocalDate.now());
    }

    @Transactional
    public void marcarAvisado(Usuario u) {
        u.setFechaUltimoAviso(LocalDateTime.now());
        repositorio.save(u);
    }
}
