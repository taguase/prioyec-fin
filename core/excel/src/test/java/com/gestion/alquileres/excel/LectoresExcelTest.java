package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.FilaInquilinoDTO;
import com.gestion.alquileres.core.dto.MovimientoDTO;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los ficheros reales mezclan formatos, asi que se comprueba la lectura tolerante. */
class LectoresExcelTest {

    @Test
    @DisplayName("la carga de inquilinos lee las 13 columnas y marca las filas invalidas")
    void leeInquilinos() throws IOException {
        byte[] libro = construir(hoja -> {
            Row cabecera = hoja.createRow(0);
            for (int c = 0; c < LectorInquilinosExcel.CABECERAS.length; c++) {
                cabecera.createCell(c).setCellValue(LectorInquilinosExcel.CABECERAS[c]);
            }
            Row valida = hoja.createRow(1);
            valida.createCell(0).setCellValue("Maria");
            valida.createCell(1).setCellValue("Lopez Garcia");
            valida.createCell(2).setCellValue("12345678Z");
            valida.createCell(3).setCellValue("01/01/2023");     // fecha como texto
            valida.createCell(4).setCellValue("31/12/2028");
            valida.createCell(5).setCellValue("Calle Mayor 10");
            valida.createCell(6).setCellValue("2");
            valida.createCell(7).setCellValue("1");
            valida.createCell(8).setCellValue("A");
            valida.createCell(9).setCellValue("Alcala de Henares");
            valida.createCell(10).setCellValue("Madrid");
            valida.createCell(11).setCellValue("1.250,50");      // importe en formato espanol
            valida.createCell(12).setCellValue("S");

            Row sinDni = hoja.createRow(2);
            sinDni.createCell(0).setCellValue("Juan");
            sinDni.createCell(1).setCellValue("Perez Sanz");
            sinDni.createCell(5).setCellValue("Calle Mayor 10");
        });

        List<FilaInquilinoDTO> filas = new LectorInquilinosExcel().leer(new ByteArrayInputStream(libro));

        assertThat(filas).hasSize(2);
        FilaInquilinoDTO primera = filas.get(0);
        assertThat(primera.isValida()).isTrue();
        assertThat(primera.getDni()).isEqualTo("12345678Z");
        assertThat(primera.getFechaInicioContrato()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(primera.getImporteRenta()).isEqualByComparingTo("1250.50");
        assertThat(primera.isInquilinoActual()).isTrue();

        assertThat(filas.get(1).isValida()).isFalse();
        assertThat(filas.get(1).getError()).contains("DNI");
    }

    @Test
    @DisplayName("el fichero mensual toma el concepto de la columna B, el importe de la C y la fecha de la D")
    void leeMovimientos() throws IOException {
        byte[] libro = construir(hoja -> {
            Workbook w = hoja.getWorkbook();
            CellStyle estiloFecha = ExcelUtil.estiloFecha(w);

            Row cabecera = hoja.createRow(0);
            for (int c = 0; c < LectorMovimientosExcel.CABECERAS.length; c++) {
                cabecera.createCell(c).setCellValue(LectorMovimientosExcel.CABECERAS[c]);
            }
            Row fila = hoja.createRow(1);
            fila.createCell(0).setCellValue("0001");
            fila.createCell(1).setCellValue("TRANSFERENCIA DE MARIA LOPEZ GARCIA ALQUILER");
            fila.createCell(2).setCellValue(750.00);
            fila.createCell(3).setCellValue(LocalDate.of(2026, 1, 5));
            fila.getCell(3).setCellStyle(estiloFecha);
        });

        List<MovimientoDTO> movimientos = new LectorMovimientosExcel().leer(new ByteArrayInputStream(libro));

        assertThat(movimientos).hasSize(1);
        MovimientoDTO m = movimientos.get(0);
        assertThat(m.getConcepto()).contains("MARIA LOPEZ GARCIA");
        assertThat(m.getImporte()).isEqualByComparingTo(new BigDecimal("750.00"));
        assertThat(m.getFecha()).isEqualTo(LocalDate.of(2026, 1, 5));
        assertThat(new LectorMovimientosExcel().periodoPredominante(movimientos)).containsExactly(2026, 1);
    }

    private interface Relleno { void aplicar(Sheet hoja); }

    private static byte[] construir(Relleno relleno) throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            relleno.aplicar(libro.createSheet("Datos"));
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
