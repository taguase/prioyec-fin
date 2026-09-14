package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.InformeMensualFilaDTO;
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
 * FUNCIONALIDAD 5 - Excel mensual: direccion del inmueble, inquilino, importe
 * de la renta del mes y fecha en la que se cobro.
 */
@Component
public class EscritorInformeMensualExcel {

    private static final String[] CABECERA = {
            "Direccion del inmueble", "Inquilino", "DNI",
            "Renta contrato", "Importe del mes", "Fecha(s) del cobro", "Origen"
    };

    public byte[] escribir(int anio, int mes, List<InformeMensualFilaDTO> filas) throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle titulo = ExcelUtil.estiloTitulo(libro);
            CellStyle cabecera = ExcelUtil.estiloCabecera(libro);
            CellStyle importe = ExcelUtil.estiloImporte(libro);

            Sheet hoja = libro.createSheet("Rentas %02d-%d".formatted(mes, anio));
            int f = 0;

            Row cab = hoja.createRow(f++);
            cab.createCell(0).setCellValue("Rentas cobradas - %s de %d".formatted(NombresMes.de(mes), anio));
            cab.getCell(0).setCellStyle(titulo);
            f++;

            Row filaCabecera = hoja.createRow(f++);
            for (int c = 0; c < CABECERA.length; c++) {
                filaCabecera.createCell(c).setCellValue(CABECERA[c]);
                filaCabecera.getCell(c).setCellStyle(cabecera);
            }

            BigDecimal total = BigDecimal.ZERO;
            for (InformeMensualFilaDTO fila : filas) {
                Row r = hoja.createRow(f++);
                ExcelUtil.escribir(r, 0, fila.direccionCompleta());
                ExcelUtil.escribir(r, 1, fila.inquilino());
                ExcelUtil.escribir(r, 2, fila.dni());
                ExcelUtil.escribir(r, 3, fila.rentaContrato(), importe);
                ExcelUtil.escribir(r, 4, fila.importeCobrado(), importe);
                ExcelUtil.escribir(r, 5, fila.fechas());
                ExcelUtil.escribir(r, 6, fila.origen());
                if (fila.importeCobrado() != null) total = total.add(fila.importeCobrado());
            }

            Row filaTotal = hoja.createRow(f);
            filaTotal.createCell(3).setCellValue("TOTAL");
            filaTotal.getCell(3).setCellStyle(cabecera);
            ExcelUtil.escribir(filaTotal, 4, total, importe);

            ExcelUtil.autoajustar(hoja, CABECERA.length);
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
