package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.MovimientoDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * FUNCIONALIDAD 3 - Lector del fichero mensual (extracto de cobros).
 *
 * <p>Layout esperado: la <b>segunda</b> columna (B) contiene el texto libre en
 * el que hay que localizar el nombre del inquilino, la <b>tercera</b> (C) el
 * importe y la <b>cuarta</b> (D) la fecha. La columna A se ignora.</p>
 */
@Component
public class LectorMovimientosExcel {

    public static final int COL_CONCEPTO = 1;
    public static final int COL_IMPORTE = 2;
    public static final int COL_FECHA = 3;

    public static final String[] CABECERAS = {
            "Referencia", "Concepto (contiene el nombre del inquilino)", "Importe", "Fecha"
    };

    public List<MovimientoDTO> leer(InputStream entrada) throws IOException {
        List<MovimientoDTO> movimientos = new ArrayList<>();
        try (Workbook libro = WorkbookFactory.create(entrada)) {
            Sheet hoja = libro.getSheetAt(0);
            for (int i = hoja.getFirstRowNum() + 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (ExcelUtil.filaVacia(fila, 0, COL_FECHA)) continue;
                String concepto = ExcelUtil.texto(fila, COL_CONCEPTO);
                BigDecimal importe = ExcelUtil.importe(fila, COL_IMPORTE);
                LocalDate fecha = ExcelUtil.fecha(fila, COL_FECHA);
                if (concepto.isBlank() && importe == null) continue;
                movimientos.add(new MovimientoDTO(i + 1, concepto, importe, fecha));
            }
        }
        return movimientos;
    }

    /**
     * Periodo (anio y mes) mas frecuente entre las fechas del fichero. Sirve
     * para proponer por defecto el mes al que imputar la carga.
     */
    public int[] periodoPredominante(List<MovimientoDTO> movimientos) {
        java.util.Map<String, Integer> conteo = new java.util.HashMap<>();
        for (MovimientoDTO m : movimientos) {
            if (m.getFecha() == null) continue;
            String clave = m.getFecha().getYear() + "-" + m.getFecha().getMonthValue();
            conteo.merge(clave, 1, Integer::sum);
        }
        LocalDate hoy = LocalDate.now();
        return conteo.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(e -> {
                    String[] p = e.getKey().split("-");
                    return new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1])};
                })
                .orElseGet(() -> new int[]{hoy.getYear(), hoy.getMonthValue()});
    }
}
