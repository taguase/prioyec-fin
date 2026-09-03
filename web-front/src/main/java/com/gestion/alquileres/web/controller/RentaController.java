package com.gestion.alquileres.web.controller;

import com.gestion.alquileres.core.model.Inmueble;
import com.gestion.alquileres.core.service.BusquedaService;
import com.gestion.alquileres.core.service.RentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * FUNCIONALIDAD 4 - Alta manual de la renta del mes.
 *
 * <p>Reutiliza la misma mecanica de busqueda de la funcionalidad 2 y anade los
 * dos campos de alta: importe y fecha. El periodo se deduce de la fecha.</p>
 */
@Controller
public class RentaController {

    private final BusquedaService busqueda;
    private final RentaService rentas;

    public RentaController(BusquedaService busqueda, RentaService rentas) {
        this.busqueda = busqueda;
        this.rentas = rentas;
    }

    @GetMapping("/rentas/alta")
    public String formulario(@RequestParam(defaultValue = BusquedaController.MODO_DOMICILIO) String modo,
                             @RequestParam(required = false) String edificio,
                             @RequestParam(required = false) Long inmuebleId,
                             @RequestParam(required = false) String texto,
                             Model model) {
        prepararModelo(modo, edificio, inmuebleId, texto, model);
        return "rentas/alta";
    }

    @PostMapping("/rentas/alta")
    public String registrar(@RequestParam Long contratoId,
                            @RequestParam BigDecimal importe,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            @RequestParam(defaultValue = BusquedaController.MODO_DOMICILIO) String modo,
                            @RequestParam(required = false) String edificio,
                            @RequestParam(required = false) Long inmuebleId,
                            @RequestParam(required = false) String texto,
                            Model model) {
        try {
            rentas.registrar(contratoId, importe, fecha);
            model.addAttribute("mensajeOk", "Renta de %02d/%d registrada correctamente (%s €)."
                    .formatted(fecha.getMonthValue(), fecha.getYear(), importe));
        } catch (Exception e) {
            model.addAttribute("mensajeError", "No se ha podido registrar la renta: " + e.getMessage());
        }
        prepararModelo(modo, edificio, inmuebleId, texto, model);
        model.addAttribute("historicoRentas", rentas.historicoDeContrato(contratoId));
        return "rentas/alta";
    }

    private void prepararModelo(String modo, String edificio, Long inmuebleId, String texto, Model model) {
        model.addAttribute("modo", modo);
        model.addAttribute("edificios", busqueda.listarEdificios());
        model.addAttribute("edificioSeleccionado", edificio);
        model.addAttribute("inmuebleId", inmuebleId);
        model.addAttribute("texto", texto);
        model.addAttribute("hoy", LocalDate.now());

        if (BusquedaController.MODO_INQUILINO.equals(modo)) {
            if (texto != null && !texto.isBlank()) {
                model.addAttribute("resultados", busqueda.buscarPorInquilino(texto));
                model.addAttribute("buscado", true);
            }
            return;
        }
        List<Inmueble> viviendas = busqueda.listarViviendas(edificio);
        model.addAttribute("viviendas", viviendas);
        if (inmuebleId != null) {
            model.addAttribute("buscado", true);
            model.addAttribute("resultados",
                    busqueda.inquilinoActual(inmuebleId).map(List::of).orElse(List.of()));
        }
    }
}
