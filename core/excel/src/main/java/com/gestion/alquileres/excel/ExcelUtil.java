package com.gestion.alquileres.excel;

import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilidades de lectura tolerante de celdas: los ficheros que envian las
 * gestorias mezclan numeros, fechas y textos en la misma columna, asi que cada
 * getter acepta los formatos habituales antes de rendirse.
 */
public final class ExcelUtil {

    private ExcelUtil() { }

    private static final List<DateTimeFormatter> FORMATOS_FECHA = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("ddMMyyyy"));

    private static final DataFormatter FORMATEADOR = new DataFormatter();

    public static String texto(Row fila, int columna) {
        Cell celda = celda(fila, columna);
        if (celda == null) return "";
        if (celda.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            LocalDate f = celda.getLocalDateTimeCellValue().toLocalDate();
            return f.format(FORMATOS_FECHA.get(0));
        }
        return FORMATEADOR.formatCellValue(celda).trim();
    }

    /** Importe en euros. Acepta 1.234,56 / 1,234.56 / 1234.56 y el signo delante o detras. */
    public static BigDecimal importe(Row fila, int columna) {
        Cell celda = celda(fila, columna);
        if (celda == null) return null;
        if (celda.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(celda.getNumericCellValue()).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        String bruto = FORMATEADOR.formatCellValue(celda).trim()
                .replace("€", "").replace("EUR", "").replaceAll("\\s", "");
        if (bruto.isEmpty()) return null;
        boolean negativo = bruto.startsWith("-") || bruto.endsWith("-");
        bruto = bruto.replace("-", "");
        int ultimaComa = bruto.lastIndexOf(',');
        int ultimoPunto = bruto.lastIndexOf('.');
        if (ultimaComa > ultimoPunto) {
            bruto = bruto.replace(".", "").replace(',', '.');   // formato espanol
        } else {
            bruto = bruto.replace(",", "");                     // formato anglosajon
        }
        try {
            BigDecimal valor = new BigDecimal(bruto).setScale(2, java.math.RoundingMode.HALF_UP);
            return negativo ? valor.negate() : valor;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Fecha. Acepta celda de tipo fecha, numero serie de Excel o texto en los formatos habituales. */
    public static LocalDate fecha(Row fila, int columna) {
        Cell celda = celda(fila, columna);
        if (celda == null) return null;
        if (celda.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(celda)) return celda.getLocalDateTimeCellValue().toLocalDate();
            double serie = celda.getNumericCellValue();
            if (serie > 0) return DateUtil.getJavaDate(serie).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            return null;
        }
        String bruto = FORMATEADOR.formatCellValue(celda).trim();
        if (bruto.isEmpty()) return null;
        for (DateTimeFormatter f : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(bruto, f);
            } catch (Exception ignored) {
                // se prueba el siguiente formato
            }
        }
        return null;
    }

    /** Marca de si/no. Se consideran verdaderos: S, SI, X, Y, YES, TRUE, 1, ACTUAL. */
    public static boolean marca(Row fila, int columna) {
        Cell celda = celda(fila, columna);
        if (celda == null) return false;
        if (celda.getCellType() == CellType.BOOLEAN) return celda.getBooleanCellValue();
        String v = FORMATEADOR.formatCellValue(celda).trim().toUpperCase();
        return v.equals("S") || v.equals("SI") || v.equals("SÍ") || v.equals("X") || v.equals("Y")
                || v.equals("YES") || v.equals("TRUE") || v.equals("1") || v.equals("ACTUAL");
    }

    public static boolean filaVacia(Row fila, int primeraColumna, int ultimaColumna) {
        if (fila == null) return true;
        for (int c = primeraColumna; c <= ultimaColumna; c++) {
            if (!texto(fila, c).isBlank()) return false;
        }
        return true;
    }

    private static Cell celda(Row fila, int columna) {
        return fila == null ? null : fila.getCell(columna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    }

    // ---------------------------------------------------------------- estilos

    public static CellStyle estiloCabecera(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        return estilo;
    }

    public static CellStyle estiloTitulo(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 13);
        estilo.setFont(fuente);
        return estilo;
    }

    public static CellStyle estiloImporte(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat("#,##0.00 \"€\""));
        return estilo;
    }

    public static CellStyle estiloFecha(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy"));
        return estilo;
    }

    public static CellStyle estiloAviso(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.DARK_RED.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    public static void escribir(Row fila, int columna, String valor) {
        fila.createCell(columna).setCellValue(valor == null ? "" : valor);
    }

    public static void escribir(Row fila, int columna, BigDecimal valor, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        c.setCellValue(valor == null ? 0d : valor.doubleValue());
        if (estilo != null) c.setCellStyle(estilo);
    }

    public static void escribir(Row fila, int columna, LocalDate valor, CellStyle estilo) {
        Cell c = fila.createCell(columna);
        if (valor != null) {
            c.setCellValue(valor);
            if (estilo != null) c.setCellStyle(estilo);
        }
    }

    /** Ajusta el ancho de las primeras columnas con un maximo razonable. */
    public static void autoajustar(Sheet hoja, int numeroColumnas) {
        for (int c = 0; c < numeroColumnas; c++) {
            hoja.autoSizeColumn(c);
            int ancho = hoja.getColumnWidth(c);
            if (ancho > 60 * 256) hoja.setColumnWidth(c, 60 * 256);
            if (ancho < 10 * 256) hoja.setColumnWidth(c, 10 * 256);
        }
    }
}
