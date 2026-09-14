package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.LineaConciliacionDTO;
import com.gestion.alquileres.core.dto.MovimientoDTO;
import com.gestion.alquileres.core.dto.ResultadoConciliacionDTO;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * FUNCIONALIDAD 3 - Excel de salida de la conciliacion.
 *
 * <p>Primero el listado de todos los inmuebles e inquilinos con el importe y las
 * fechas encontradas en el fichero, y a continuacion el bloque con los
 * movimientos del fichero que no han casado con ningun inquilino.</p>
 */
@Component
public class EscritorConciliacionExcel {

    private static final String[] CABECERA_LISTADO = {
            "Direccion del inmueble", "Nombre", "Apellidos", "DNI",
            "Renta contrato", "Importe encontrado", "Fecha(s) del cobro", "Nº movimientos", "Diferencia"
    };

    private static final String[] CABECERA_NO_CASADOS = {
            "Fila fichero", "Concepto", "Importe", "Fecha", "Motivo"
    };

    public byte[] escribir(ResultadoConciliacionDTO resultado) throws IOException {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle titulo = ExcelUtil.estiloTitulo(libro);
            CellStyle cabecera = ExcelUtil.estiloCabecera(libro);
            CellStyle importe = ExcelUtil.estiloImporte(libro);
            CellStyle fecha = ExcelUtil.estiloFecha(libro);
            CellStyle aviso = ExcelUtil.estiloAviso(libro);

            Sheet hoja = libro.createSheet("Conciliacion");
            int f = 0;

            Row cabeceraInforme = hoja.createRow(f++);
            cabeceraInforme.createCell(0).setCellValue(
                    "Conciliacion de cobros %02d/%d - fichero %s"
                            .formatted(resultado.getMes(), resultado.getAnio(), resultado.getNombreFichero()));
            cabeceraInforme.getCell(0).setCellStyle(titulo);

            Row resumen = hoja.createRow(f++);
            resumen.createCell(0).setCellValue(
                    "Movimientos leidos: %d   |   Contratos con cobro: %d   |   Contratos sin cobro: %d   |   Movimientos sin casar: %d"
                            .formatted(resultado.getMovimientosLeidos(), resultado.getContratosConCobro(),
                                    resultado.getContratosSinCobro(), resultado.getNoEncontrados().size()));
            f++;

            Row filaCabecera = hoja.createRow(f++);
            for (int c = 0; c < CABECERA_LISTADO.length; c++) {
                filaCabecera.createCell(c).setCellValue(CABECERA_LISTADO[c]);
                filaCabecera.getCell(c).setCellStyle(cabecera);
            }

            for (LineaConciliacionDTO linea : resultado.getLineas()) {
                Row fila = hoja.createRow(f++);
                ExcelUtil.escribir(fila, 0, linea.getDireccionCompleta());
                ExcelUtil.escribir(fila, 1, linea.getNombre());
                ExcelUtil.escribir(fila, 2, linea.getApellidos());
                ExcelUtil.escribir(fila, 3, linea.getDni());
                ExcelUtil.escribir(fila, 4, linea.getRentaContrato(), importe);
                ExcelUtil.escribir(fila, 5, linea.getImporteEncontrado(), importe);
                ExcelUtil.escribir(fila, 6, linea.getFechas());
                fila.createCell(7).setCellValue(linea.getNumMovimientos());
                BigDecimal diferencia = nz(linea.getImporteEncontrado()).subtract(nz(linea.getRentaContrato()));
                ExcelUtil.escribir(fila, 8, diferencia, importe);
            }

            // ---- Bloque final: movimientos del fichero que no han casado -----
            f += 2;
            Row tituloNoCasados = hoja.createRow(f++);
            tituloNoCasados.createCell(0).setCellValue(
                    "MOVIMIENTOS DEL FICHERO NO LOCALIZADOS (" + resultado.getNoEncontrados().size() + ")");
            tituloNoCasados.getCell(0).setCellStyle(aviso);

            Row cabeceraNoCasados = hoja.createRow(f++);
            for (int c = 0; c < CABECERA_NO_CASADOS.length; c++) {
                cabeceraNoCasados.createCell(c).setCellValue(CABECERA_NO_CASADOS[c]);
                cabeceraNoCasados.getCell(c).setCellStyle(cabecera);
            }

            for (MovimientoDTO m : resultado.getNoEncontrados()) {
                Row fila = hoja.createRow(f++);
                fila.createCell(0).setCellValue(m.getFila());
                ExcelUtil.escribir(fila, 1, m.getConcepto());
                ExcelUtil.escribir(fila, 2, m.getImporte(), importe);
                ExcelUtil.escribir(fila, 3, m.getFecha(), fecha);
                ExcelUtil.escribir(fila, 4, m.getMotivo());
            }

            ExcelUtil.autoajustar(hoja, CABECERA_LISTADO.length);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
