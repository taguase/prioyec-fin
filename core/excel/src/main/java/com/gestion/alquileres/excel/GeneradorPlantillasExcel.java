package com.gestion.alquileres.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Genera las plantillas Excel vacias (con cabecera y una fila de ejemplo) que
 * los usuarios pueden descargarse desde las pantallas de carga.
 */
@Component
public class GeneradorPlantillasExcel {

    /** Plantilla de la carga de inquilinos (funcionalidad 1). */
    public byte[] plantillaInquilinos() throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Inquilinos");
            CellStyle cabecera = ExcelUtil.estiloCabecera(libro);
            CellStyle fecha = ExcelUtil.estiloFecha(libro);
            CellStyle importe = ExcelUtil.estiloImporte(libro);

            Row filaCabecera = hoja.createRow(0);
            for (int c = 0; c < LectorInquilinosExcel.CABECERAS.length; c++) {
                filaCabecera.createCell(c).setCellValue(LectorInquilinosExcel.CABECERAS[c]);
                filaCabecera.getCell(c).setCellStyle(cabecera);
            }

            Row ejemplo = hoja.createRow(1);
            ExcelUtil.escribir(ejemplo, 0, "Maria");
            ExcelUtil.escribir(ejemplo, 1, "Lopez Garcia");
            ExcelUtil.escribir(ejemplo, 2, "12345678Z");
            ExcelUtil.escribir(ejemplo, 3, LocalDate.of(2023, 1, 1), fecha);
            ExcelUtil.escribir(ejemplo, 4, LocalDate.of(2028, 12, 31), fecha);
            ExcelUtil.escribir(ejemplo, 5, "Calle Mayor 10");
            ExcelUtil.escribir(ejemplo, 6, "2");
            ExcelUtil.escribir(ejemplo, 7, "3");
            ExcelUtil.escribir(ejemplo, 8, "B");
            ExcelUtil.escribir(ejemplo, 9, "Alcala de Henares");
            ExcelUtil.escribir(ejemplo, 10, "Madrid");
            ExcelUtil.escribir(ejemplo, 11, new java.math.BigDecimal("750.00"), importe);
            ExcelUtil.escribir(ejemplo, 12, "S");

            ExcelUtil.autoajustar(hoja, LectorInquilinosExcel.CABECERAS.length);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    /** Plantilla del fichero mensual de cobros (funcionalidad 3). */
    public byte[] plantillaMovimientos() throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Movimientos");
            CellStyle cabecera = ExcelUtil.estiloCabecera(libro);
            CellStyle fecha = ExcelUtil.estiloFecha(libro);
            CellStyle importe = ExcelUtil.estiloImporte(libro);

            Row filaCabecera = hoja.createRow(0);
            for (int c = 0; c < LectorMovimientosExcel.CABECERAS.length; c++) {
                filaCabecera.createCell(c).setCellValue(LectorMovimientosExcel.CABECERAS[c]);
                filaCabecera.getCell(c).setCellStyle(cabecera);
            }

            Row ejemplo = hoja.createRow(1);
            ExcelUtil.escribir(ejemplo, 0, "0001");
            ExcelUtil.escribir(ejemplo, 1, "TRANSFERENCIA DE MARIA LOPEZ GARCIA CONCEPTO ALQUILER ENERO");
            ExcelUtil.escribir(ejemplo, 2, new java.math.BigDecimal("750.00"), importe);
            ExcelUtil.escribir(ejemplo, 3, LocalDate.of(2026, 1, 5), fecha);

            ExcelUtil.autoajustar(hoja, LectorMovimientosExcel.CABECERAS.length);
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
