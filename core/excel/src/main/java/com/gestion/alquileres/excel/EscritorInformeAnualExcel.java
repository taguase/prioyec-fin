package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.InformeAnualFilaDTO;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * FUNCIONALIDAD 6 - Excel anual: por cada inmueble, la direccion, el listado de
 * inquilinos (nombre y DNI) y los importes de los doce meses en columnas
 * consecutivas.
 */
@Component
public class EscritorInformeAnualExcel {

    public byte[] escribir(int anio, List<InformeAnualFilaDTO> filas) throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle titulo = ExcelUtil.estiloTitulo(libro);
            CellStyle cabecera = ExcelUtil.estiloCabecera(libro);
            CellStyle importe = ExcelUtil.estiloImporte(libro);

            Sheet hoja = libro.createSheet("Resumen " + anio);
            int f = 0;

            Row cab = hoja.createRow(f++);
            cab.createCell(0).setCellValue("Resumen anual de rentas - " + anio);
            cab.getCell(0).setCellStyle(titulo);
            f++;

            Row filaCabecera = hoja.createRow(f++);
            String[] fijas = {"Direccion del inmueble", "Inquilinos", "DNI"};
            for (int c = 0; c < fijas.length; c++) {
                filaCabecera.createCell(c).setCellValue(fijas[c]);
                filaCabecera.getCell(c).setCellStyle(cabecera);
            }
            for (int m = 0; m < 12; m++) {
                filaCabecera.createCell(fijas.length + m).setCellValue(NombresMes.NOMBRES[m]);
                filaCabecera.getCell(fijas.length + m).setCellStyle(cabecera);
            }
            filaCabecera.createCell(fijas.length + 12).setCellValue("Total anio");
            filaCabecera.getCell(fijas.length + 12).setCellStyle(cabecera);

            BigDecimal[] totalesMes = new BigDecimal[12];
            for (int m = 0; m < 12; m++) totalesMes[m] = BigDecimal.ZERO;
            BigDecimal totalGeneral = BigDecimal.ZERO;

            for (InformeAnualFilaDTO fila : filas) {
                Row r = hoja.createRow(f++);
                ExcelUtil.escribir(r, 0, fila.getDireccionCompleta());
                ExcelUtil.escribir(r, 1, fila.getInquilinos());
                ExcelUtil.escribir(r, 2, fila.getDnis());
                for (int m = 0; m < 12; m++) {
                    ExcelUtil.escribir(r, fijas.length + m, fila.getImportes()[m], importe);
                    totalesMes[m] = totalesMes[m].add(fila.getImportes()[m]);
                }
                ExcelUtil.escribir(r, fijas.length + 12, fila.getTotal(), importe);
                totalGeneral = totalGeneral.add(fila.getTotal());
            }

            Row filaTotal = hoja.createRow(f);
            filaTotal.createCell(0).setCellValue("TOTAL");
            filaTotal.getCell(0).setCellStyle(cabecera);
            for (int m = 0; m < 12; m++) {
                ExcelUtil.escribir(filaTotal, fijas.length + m, totalesMes[m], importe);
            }
            ExcelUtil.escribir(filaTotal, fijas.length + 12, totalGeneral, importe);

            hoja.createFreezePane(3, 3);
            ExcelUtil.autoajustar(hoja, fijas.length + 13);
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
