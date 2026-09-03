package com.gestion.alquileres.web.controller;

import com.gestion.alquileres.core.service.InformeService;
import com.gestion.alquileres.excel.EscritorInformeAnualExcel;
import com.gestion.alquileres.excel.EscritorInformeMensualExcel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

/**
 * FUNCIONALIDADES 5 y 6 - Descarga de los informes Excel mensual y anual.
 */
@Controller
public class InformesController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final InformeService informes;
    private final EscritorInformeMensualExcel escritorMensual;
    private final EscritorInformeAnualExcel escritorAnual;

    public InformesController(InformeService informes, EscritorInformeMensualExcel escritorMensual,
                              EscritorInformeAnualExcel escritorAnual) {
        this.informes = informes;
        this.escritorMensual = escritorMensual;
        this.escritorAnual = escritorAnual;
    }

    @GetMapping("/informes")
    public String pantalla(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("anio", hoy.getYear());
        model.addAttribute("mes", hoy.getMonthValue());
        model.addAttribute("previoMensual", informes.datosMensuales(hoy.getYear(), hoy.getMonthValue()));
        return "informes/informes";
    }

    /** FUNCIONALIDAD 5 - Excel mensual. */
    @GetMapping("/informes/mensual")
    public ResponseEntity<byte[]> mensual(@RequestParam int anio, @RequestParam int mes) throws IOException {
        byte[] contenido = escritorMensual.escribir(anio, mes, informes.datosMensuales(anio, mes));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"rentas-%d-%02d.xlsx\"".formatted(anio, mes))
                .contentType(MediaType.parseMediaType(XLSX))
                .body(contenido);
    }

    /** FUNCIONALIDAD 6 - Excel anual. */
    @GetMapping("/informes/anual")
    public ResponseEntity<byte[]> anual(@RequestParam int anio) throws IOException {
        byte[] contenido = escritorAnual.escribir(anio, informes.datosAnuales(anio));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"rentas-anual-%d.xlsx\"".formatted(anio))
                .contentType(MediaType.parseMediaType(XLSX))
                .body(contenido);
    }
}
