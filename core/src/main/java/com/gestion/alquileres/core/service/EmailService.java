package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.config.AppProperties;
import com.gestion.alquileres.core.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Envio de los avisos de contrasena.
 *
 * <p>El destinatario es el correo grabado en la ficha del usuario
 * ({@code usuario.email}). Si el envio esta deshabilitado o no hay servidor SMTP
 * configurado, el aviso se registra en el log en lugar de fallar: la aplicacion
 * nunca deja de funcionar por un problema de correo.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObjectProvider<JavaMailSender> mailSender;
    private final AppProperties propiedades;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, AppProperties propiedades) {
        this.mailSender = mailSender;
        this.propiedades = propiedades;
    }

    /** Aviso de contrasena ya caducada. */
    public boolean enviarAvisoCaducada(Usuario usuario) {
        String cuerpo = """
                Estimado/a %s:

                La contrasena de su usuario "%s" caduco el %s, por lo que su acceso a la
                aplicacion de Gestion de Alquileres esta bloqueado.

                Pongase en contacto con el administrador para restablecerla.

                Este es un mensaje automatico, por favor no responda a este correo.
                """.formatted(
                nombreDe(usuario),
                usuario.getUsername(),
                usuario.getFechaCaducidadPassword() == null ? "-" : usuario.getFechaCaducidadPassword().format(FECHA));
        return enviar(usuario.getEmail(), propiedades.getCorreo().getAsuntoCaducada(), cuerpo);
    }

    /** Preaviso: faltan pocos dias para caducar. */
    public boolean enviarPreaviso(Usuario usuario, long diasRestantes) {
        String cuerpo = """
                Estimado/a %s:

                La contrasena de su usuario "%s" caducara el %s (faltan %d dia(s)).
                Cambiela antes de esa fecha para no perder el acceso.

                Este es un mensaje automatico, por favor no responda a este correo.
                """.formatted(
                nombreDe(usuario),
                usuario.getUsername(),
                usuario.getFechaCaducidadPassword() == null ? "-" : usuario.getFechaCaducidadPassword().format(FECHA),
                diasRestantes);
        return enviar(usuario.getEmail(), propiedades.getCorreo().getAsuntoPreaviso(), cuerpo);
    }

    private String nombreDe(Usuario u) {
        return u.getNombreCompleto() == null || u.getNombreCompleto().isBlank()
                ? u.getUsername() : u.getNombreCompleto();
    }

    private boolean enviar(String destinatario, String asunto, String cuerpo) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("El usuario no tiene correo grabado; no se envia el aviso '{}'", asunto);
            return false;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!propiedades.getCorreo().isHabilitado() || sender == null) {
            log.info("[CORREO SIMULADO] para={} asunto={}\n{}", destinatario, asunto, cuerpo);
            return false;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(propiedades.getCorreo().getRemitente());
            mensaje.setTo(destinatario);
            String copia = propiedades.getCorreo().getCopiaAdministrador();
            if (copia != null && !copia.isBlank()) mensaje.setCc(copia);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            sender.send(mensaje);
            log.info("Aviso '{}' enviado a {}", asunto, destinatario);
            return true;
        } catch (Exception e) {
            log.error("No se ha podido enviar el aviso '{}' a {}: {}", asunto, destinatario, e.getMessage());
            return false;
        }
    }
}
