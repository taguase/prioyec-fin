package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.config.AppProperties;
import com.gestion.alquileres.core.model.Usuario;
import com.gestion.alquileres.core.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Tarea diaria que revisa las contrasenas: envia el aviso a las ya caducadas y
 * el preaviso a las que caducan dentro de la ventana configurada. El correo se
 * manda al buzon grabado en la ficha de cada usuario.
 *
 * <p>La cuenta {@code system} queda fuera por construccion, ya que las consultas
 * excluyen las cuentas con {@code passwordNuncaCaduca = true}.</p>
 */
@Component
public class CaducidadPasswordScheduler {

    private static final Logger log = LoggerFactory.getLogger(CaducidadPasswordScheduler.class);

    private final UsuarioRepository repositorio;
    private final EmailService emailService;
    private final AppProperties propiedades;

    public CaducidadPasswordScheduler(UsuarioRepository repositorio, EmailService emailService,
                                      AppProperties propiedades) {
        this.repositorio = repositorio;
        this.emailService = emailService;
        this.propiedades = propiedades;
    }

    @Scheduled(cron = "${app.password.cron-revision:0 0 7 * * *}")
    @Transactional
    public void revisarCaducidades() {
        LocalDate hoy = LocalDate.now();
        int avisos = 0;

        for (Usuario u : repositorio.findConPasswordCaducada(hoy)) {
            if (yaAvisadoHoy(u, hoy)) continue;
            emailService.enviarAvisoCaducada(u);
            u.setFechaUltimoAviso(hoy.atStartOfDay());
            repositorio.save(u);
            avisos++;
        }

        LocalDate limite = hoy.plusDays(propiedades.getPassword().getDiasPreaviso());
        List<Usuario> proximos = repositorio
                .findByActivoTrueAndPasswordNuncaCaducaFalseAndFechaCaducidadPasswordBetween(hoy, limite);
        for (Usuario u : proximos) {
            if (yaAvisadoHoy(u, hoy)) continue;
            long dias = ChronoUnit.DAYS.between(hoy, u.getFechaCaducidadPassword());
            emailService.enviarPreaviso(u, dias);
            u.setFechaUltimoAviso(hoy.atStartOfDay());
            repositorio.save(u);
            avisos++;
        }

        log.info("Revision de caducidad de contrasenas completada: {} aviso(s) enviado(s)", avisos);
    }

    private boolean yaAvisadoHoy(Usuario u, LocalDate hoy) {
        return u.getFechaUltimoAviso() != null && u.getFechaUltimoAviso().toLocalDate().isEqual(hoy);
    }
}
