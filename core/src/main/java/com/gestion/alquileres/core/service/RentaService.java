package com.gestion.alquileres.core.service;

import com.gestion.alquileres.core.model.Contrato;
import com.gestion.alquileres.core.model.OrigenRenta;
import com.gestion.alquileres.core.model.RentaMensual;
import com.gestion.alquileres.core.repository.ContratoRepository;
import com.gestion.alquileres.core.repository.RentaMensualRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * FUNCIONALIDAD 4 - Alta manual de la renta del mes.
 *
 * <p>Se localiza el contrato con la misma pantalla de busqueda y se introducen
 * importe y fecha. El periodo (anio/mes) se deduce de la fecha indicada.
 * Si ya existe un registro para ese contrato y periodo se actualiza.</p>
 */
@Service
public class RentaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RentaMensualRepository rentas;
    private final ContratoRepository contratos;

    public RentaService(RentaMensualRepository rentas, ContratoRepository contratos) {
        this.rentas = rentas;
        this.contratos = contratos;
    }

    @Transactional
    public RentaMensual registrar(Long contratoId, BigDecimal importe, LocalDate fecha) {
        Contrato contrato = contratos.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado: " + contratoId));
        if (importe == null) throw new IllegalArgumentException("El importe de la renta es obligatorio");
        if (fecha == null) throw new IllegalArgumentException("La fecha de la renta es obligatoria");

        int anio = fecha.getYear();
        int mes = fecha.getMonthValue();

        RentaMensual renta = rentas.findByContratoIdAndAnioAndMes(contratoId, anio, mes)
                .orElseGet(() -> {
                    RentaMensual nueva = new RentaMensual();
                    nueva.setContrato(contrato);
                    nueva.setAnio(anio);
                    nueva.setMes(mes);
                    return nueva;
                });
        renta.setImporte(importe);
        renta.setFechaPago(fecha);
        renta.setFechasPago(fecha.format(FECHA));
        renta.setNumMovimientos(1);
        renta.setOrigen(OrigenRenta.MANUAL);
        renta.setCargaId(null);
        return rentas.save(renta);
    }

    @Transactional(readOnly = true)
    public List<RentaMensual> historicoDeContrato(Long contratoId) {
        return rentas.findByContratoIdOrderByAnioDescMesDesc(contratoId);
    }

    @Transactional(readOnly = true)
    public Optional<RentaMensual> buscar(Long contratoId, int anio, int mes) {
        return rentas.findByContratoIdAndAnioAndMes(contratoId, anio, mes);
    }
}
