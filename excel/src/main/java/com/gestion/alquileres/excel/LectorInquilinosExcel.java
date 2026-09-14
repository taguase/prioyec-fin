package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.FilaInquilinoDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * FUNCIONALIDAD 1 - Lector del Excel de inquilinos.
 *
 * <p>Layout esperado (la primera fila es la cabecera y se ignora):</p>
 * <pre>
 *  A Nombre            E Fecha fin contrato   I Letra              M Inquilino actual (S/N)
 *  B Apellidos         F Nombre de la calle   J Municipio
 *  C DNI               G Portal               K Ciudad
 *  D Fecha inicio      H Piso                 L Importe renta actual
 * </pre>
 */
@Component
public class LectorInquilinosExcel {

    public static final int COL_NOMBRE = 0;
    public static final int COL_APELLIDOS = 1;
    public static final int COL_DNI = 2;
    public static final int COL_FECHA_INICIO = 3;
    public static final int COL_FECHA_FIN = 4;
    public static final int COL_CALLE = 5;
    public static final int COL_PORTAL = 6;
    public static final int COL_PISO = 7;
    public static final int COL_LETRA = 8;
    public static final int COL_MUNICIPIO = 9;
    public static final int COL_CIUDAD = 10;
    public static final int COL_RENTA = 11;
    public static final int COL_ACTUAL = 12;

    public static final String[] CABECERAS = {
            "Nombre", "Apellidos", "DNI", "Fecha inicio contrato", "Fecha fin contrato",
            "Nombre de la calle", "Portal", "Piso", "Letra", "Municipio", "Ciudad",
            "Importe renta actual", "Inquilino actual (S/N)"
    };

    public List<FilaInquilinoDTO> leer(InputStream entrada) throws IOException {
        List<FilaInquilinoDTO> filas = new ArrayList<>();
        try (Workbook libro = WorkbookFactory.create(entrada)) {
            Sheet hoja = libro.getSheetAt(0);
            for (int i = hoja.getFirstRowNum() + 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (ExcelUtil.filaVacia(fila, COL_NOMBRE, COL_ACTUAL)) continue;
                filas.add(leerFila(fila, i + 1));
            }
        }
        return filas;
    }

    private FilaInquilinoDTO leerFila(Row fila, int numeroFila) {
        FilaInquilinoDTO dto = new FilaInquilinoDTO();
        dto.setFila(numeroFila);
        dto.setNombre(ExcelUtil.texto(fila, COL_NOMBRE));
        dto.setApellidos(ExcelUtil.texto(fila, COL_APELLIDOS));
        dto.setDni(ExcelUtil.texto(fila, COL_DNI));
        dto.setFechaInicioContrato(ExcelUtil.fecha(fila, COL_FECHA_INICIO));
        dto.setFechaFinContrato(ExcelUtil.fecha(fila, COL_FECHA_FIN));
        dto.setNombreCalle(ExcelUtil.texto(fila, COL_CALLE));
        dto.setPortal(ExcelUtil.texto(fila, COL_PORTAL));
        dto.setPiso(ExcelUtil.texto(fila, COL_PISO));
        dto.setLetra(ExcelUtil.texto(fila, COL_LETRA));
        dto.setMunicipio(ExcelUtil.texto(fila, COL_MUNICIPIO));
        dto.setCiudad(ExcelUtil.texto(fila, COL_CIUDAD));
        dto.setImporteRenta(ExcelUtil.importe(fila, COL_RENTA));
        dto.setInquilinoActual(ExcelUtil.marca(fila, COL_ACTUAL));
        dto.setError(validar(dto));
        return dto;
    }

    private String validar(FilaInquilinoDTO d) {
        if (d.getNombre().isBlank())    return "el nombre es obligatorio";
        if (d.getApellidos().isBlank()) return "los apellidos son obligatorios";
        if (d.getDni().isBlank())       return "el DNI es obligatorio";
        if (d.getNombreCalle().isBlank()) return "el nombre de la calle es obligatorio";
        if (d.getFechaInicioContrato() == null) return "la fecha de inicio de contrato no es una fecha valida";
        if (d.getFechaFinContrato() != null && d.getFechaFinContrato().isBefore(d.getFechaInicioContrato())) {
            return "la fecha de fin es anterior a la de inicio";
        }
        if (d.getImporteRenta() == null) return "el importe de la renta no es un numero valido";
        return null;
    }
}
