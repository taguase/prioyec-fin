package com.gestion.alquileres.web.controller;

import com.gestion.alquileres.core.dto.MovimientoDTO;
import com.gestion.alquileres.core.dto.ResultadoConciliacionDTO;
import com.gestion.alquileres.core.service.ConciliacionService;
import com.gestion.alquileres.excel.EscritorConciliacionExcel;
import com.gestion.alquileres.excel.GeneradorPlantillasExcel;
import com.gestion.alquileres.excel.LectorMovimientosExcel;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * FUNCIONALIDAD 3 - Carga del fichero del mes y conciliacion.
 *
 * <p>El Excel resultante se deja en sesion para que el usuario pueda
 * descargarlo despues de revisar el resumen en pantalla.</p>
 */
@Controller
public class ConciliacionController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static final String SESION_EXCEL = "conciliacion.excel";
    static final String SESION_NOMBRE = "conciliacion.nombre";

    private final LectorMovimientosExcel lector;
    private final ConciliacionService servicio;
    private final EscritorConciliacionExcel escritor;
    private final GeneradorPlantillasExcel plantillas;

    public ConciliacionController(LectorMovimientosExcel lector, ConciliacionService servicio,
                                  EscritorConciliacionExcel escritor, GeneradorPlantillasExcel plantillas) {
        this.lector = lector;
        this.servicio = servicio;
        this.escritor = escritor;
        this.plantillas = plantillas;
    }

    @GetMapping("/rentas/carga-mensual")
    public String formulario(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("anio", hoy.getYear());
        model.addAttribute("mes", hoy.getMonthValue());
        return "rentas/carga-mensual";
    }

    @PostMapping("/rentas/carga-mensual")
    public String cargar(@RequestParam("fichero") MultipartFile fichero,
                         @RequestParam(required = false) Integer anio,
                         @RequestParam(required = false) Integer mes,
                         @AuthenticationPrincipal UserDetails usuario,
                         HttpSession sesion,
                         Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("anio", anio == null ? hoy.getYear() : anio);
        model.addAttribute("mes", mes == null ? hoy.getMonthValue() : mes);

        if (fichero == null || fichero.isEmpty()) {
            model.addAttribute("mensajeError", "Seleccione el fichero Excel del mes.");
            return "rentas/carga-mensual";
        }
        try (InputStream entrada = fichero.getInputStream()) {
            List<MovimientoDTO> movimientos = lector.leer(entrada);
            int[] periodo = lector.periodoPredominante(movimientos);
            int anioFinal = anio == null ? periodo[0] : anio;
            int mesFinal = mes == null ? periodo[1] : mes;

            ResultadoConciliacionDTO resultado = servicio.conciliar(
                    movimientos, anioFinal, mesFinal, fichero.getOriginalFilename(),
                    usuario == null ? null : usuario.getUsername());

            byte[] excel = escritor.escribir(resultado);
            sesion.setAttribute(SESION_EXCEL, excel);
            sesion.setAttribute(SESION_NOMBRE, "conciliacion-%d-%02d.xlsx".formatted(anioFinal, mesFinal));

            model.addAttribute("resultado", resultado);
            model.addAttribute("anio", anioFinal);
            model.addAttribute("mes", mesFinal);
        } catch (IOException e) {
            model.addAttribute("mensajeError", "No se ha podido leer el fichero: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error procesando el fichero: " + e.getMessage());
        }
        return "rentas/carga-mensual";
    }

    /** Descarga del Excel de conciliacion generado en la ultima carga de la sesion. */
    @GetMapping("/rentas/carga-mensual/descargar")
    public ResponseEntity<byte[]> descargar(HttpSession sesion) {
        byte[] contenido = (byte[]) sesion.getAttribute(SESION_EXCEL);
        if (contenido == null) return ResponseEntity.notFound().build();
        String nombre = (String) sesion.getAttribute(SESION_NOMBRE);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + (nombre == null ? "conciliacion.xlsx" : nombre) + "\"")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(contenido);
    }

    @GetMapping("/rentas/plantilla-mensual")
    public ResponseEntity<byte[]> plantilla() throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla-fichero-mensual.xlsx\"")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(plantillas.plantillaMovimientos());
    }
}
