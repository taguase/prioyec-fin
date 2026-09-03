package com.gestion.alquileres.web.controller;

import com.gestion.alquileres.core.dto.FilaInquilinoDTO;
import com.gestion.alquileres.core.dto.ResultadoCargaDTO;
import com.gestion.alquileres.core.service.CargaInquilinosService;
import com.gestion.alquileres.excel.GeneradorPlantillasExcel;
import com.gestion.alquileres.excel.LectorInquilinosExcel;
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
import java.util.List;

/**
 * FUNCIONALIDAD 1 - Pantalla de carga del Excel de inquilinos.
 */
@Controller
public class CargaInquilinosController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final LectorInquilinosExcel lector;
    private final CargaInquilinosService servicio;
    private final GeneradorPlantillasExcel plantillas;

    public CargaInquilinosController(LectorInquilinosExcel lector, CargaInquilinosService servicio,
                                     GeneradorPlantillasExcel plantillas) {
        this.lector = lector;
        this.servicio = servicio;
        this.plantillas = plantillas;
    }

    @GetMapping("/inquilinos/carga")
    public String formulario(Model model) {
        model.addAttribute("cabeceras", LectorInquilinosExcel.CABECERAS);
        return "inquilinos/carga";
    }

    @PostMapping("/inquilinos/carga")
    public String cargar(@RequestParam("fichero") MultipartFile fichero,
                         @AuthenticationPrincipal UserDetails usuario,
                         Model model) {
        model.addAttribute("cabeceras", LectorInquilinosExcel.CABECERAS);
        if (fichero == null || fichero.isEmpty()) {
            model.addAttribute("mensajeError", "Seleccione un fichero Excel.");
            return "inquilinos/carga";
        }
        try (InputStream entrada = fichero.getInputStream()) {
            List<FilaInquilinoDTO> filas = lector.leer(entrada);
            ResultadoCargaDTO resultado = servicio.procesar(
                    filas, fichero.getOriginalFilename(), usuario == null ? null : usuario.getUsername());
            model.addAttribute("resultado", resultado);
        } catch (IOException e) {
            model.addAttribute("mensajeError", "No se ha podido leer el fichero: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error procesando el fichero: " + e.getMessage());
        }
        return "inquilinos/carga";
    }

    @GetMapping("/inquilinos/plantilla")
    public ResponseEntity<byte[]> plantilla() throws IOException {
        byte[] contenido = plantillas.plantillaInquilinos();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla-inquilinos.xlsx\"")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(contenido);
    }
}
