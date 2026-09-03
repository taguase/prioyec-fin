package com.gestion.alquileres.web.controller;

import com.gestion.alquileres.core.dto.BusquedaResultadoDTO;
import com.gestion.alquileres.core.model.Inmueble;
import com.gestion.alquileres.core.service.BusquedaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * FUNCIONALIDAD 2 - Busqueda de inquilinos.
 *
 * <p>El radio button {@code modo} decide si la busqueda es por domicilio
 * (combo de direcciones + combo de piso/letra) o por inquilino (texto libre
 * sobre nombre o DNI, con LIKE).</p>
 */
@Controller
public class BusquedaController {

    public static final String MODO_DOMICILIO = "DOMICILIO";
    public static final String MODO_INQUILINO = "INQUILINO";

    private final BusquedaService servicio;

    public BusquedaController(BusquedaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/inquilinos/buscar")
    public String buscar(@RequestParam(defaultValue = MODO_DOMICILIO) String modo,
                         @RequestParam(required = false) String edificio,
                         @RequestParam(required = false) Long inmuebleId,
                         @RequestParam(required = false) String texto,
                         @RequestParam(defaultValue = "false") boolean verHistorico,
                         @RequestParam(defaultValue = "false") boolean verAnteriores,
                         Model model) {

        model.addAttribute("modo", modo);
        model.addAttribute("edificios", servicio.listarEdificios());
        model.addAttribute("edificioSeleccionado", edificio);
        model.addAttribute("inmuebleId", inmuebleId);
        model.addAttribute("texto", texto);
        model.addAttribute("verHistorico", verHistorico);
        model.addAttribute("verAnteriores", verAnteriores);

        if (MODO_INQUILINO.equals(modo)) {
            if (texto != null && !texto.isBlank()) {
                List<BusquedaResultadoDTO> resultados = servicio.buscarPorInquilino(texto);
                model.addAttribute("resultados", resultados);
                model.addAttribute("buscado", true);
                if (resultados.isEmpty()) {
                    model.addAttribute("mensajeInfo", "No se ha encontrado ningun inquilino con ese nombre o DNI.");
                }
            }
            return "inquilinos/buscar";
        }

        // --- Busqueda por domicilio -------------------------------------
        List<Inmueble> viviendas = servicio.listarViviendas(edificio);
        model.addAttribute("viviendas", viviendas);

        if (inmuebleId != null) {
            model.addAttribute("buscado", true);
            servicio.buscarInmueble(inmuebleId).ifPresent(i -> model.addAttribute("inmueble", i));
            var actual = servicio.inquilinoActual(inmuebleId);
            model.addAttribute("inquilinoActual", actual.orElse(null));
            if (actual.isEmpty()) {
                model.addAttribute("mensajeInfo", "La vivienda seleccionada no tiene inquilino actual.");
            }
            if (verHistorico) model.addAttribute("historico", servicio.historico(inmuebleId));
            if (verAnteriores) model.addAttribute("anteriores", servicio.anteriores(inmuebleId));
        }
        return "inquilinos/buscar";
    }

    /** Combo dependiente: viviendas del portal seleccionado. */
    @GetMapping("/inquilinos/viviendas")
    @ResponseBody
    public List<Map<String, Object>> viviendasDeEdificio(@RequestParam String edificio) {
        return servicio.listarViviendas(edificio).stream()
                .map(i -> Map.<String, Object>of(
                        "id", i.getId(),
                        "descripcion", descripcionVivienda(i)))
                .toList();
    }

    private static String descripcionVivienda(Inmueble i) {
        String piso = i.getPiso() == null || i.getPiso().isBlank() ? "-" : i.getPiso();
        String letra = i.getLetra() == null || i.getLetra().isBlank() ? "" : " " + i.getLetra();
        return piso + letra;
    }
}
