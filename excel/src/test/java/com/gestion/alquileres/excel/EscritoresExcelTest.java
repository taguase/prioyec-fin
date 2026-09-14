package com.gestion.alquileres.excel;

import com.gestion.alquileres.core.dto.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EscritoresExcelTest {

    @Test
    @DisplayName("el Excel de conciliacion lleva el listado y, detras, los movimientos no localizados")
    void conciliacion() throws IOException {
        ResultadoConciliacionDTO resultado = new ResultadoConciliacionDTO();
        resultado.setNombreFichero("cobros-2026-01.xlsx");
        resultado.setAnio(2026);
        resultado.setMes(1);
        resultado.setMovimientosLeidos(2);

        LineaConciliacionDTO linea = new LineaConciliacionDTO();
        linea.setDireccionCompleta("Calle Mayor 10, portal 2, 1 A, Alcala de Henares");
        linea.setNombre("Maria");
        linea.setApellidos("Lopez Garcia");
        linea.setDni("12345678Z");
        linea.setRentaContrato(new BigDecimal("750.00"));
        linea.setImporteEncontrado(new BigDecimal("750.00"));
        linea.setFechas("05/01/2026");
        linea.setNumMovimientos(1);
        resultado.getLineas().add(linea);

        MovimientoDTO huerfano = new MovimientoDTO(8, "TRANSFERENCIA SIN IDENTIFICAR",
                new BigDecimal("300.00"), LocalDate.of(2026, 1, 15));
        huerfano.setMotivo("No se ha localizado ningun inquilino en el concepto");
        resultado.getNoEncontrados().add(huerfano);

        byte[] salida = new EscritorConciliacionExcel().escribir(resultado);
        String contenido = volcarTexto(salida);

        assertThat(contenido).contains("Maria", "12345678Z", "Calle Mayor 10");
        assertThat(contenido).contains("MOVIMIENTOS DEL FICHERO NO LOCALIZADOS (1)");
        assertThat(contenido).contains("TRANSFERENCIA SIN IDENTIFICAR");
    }

    @Test
    @DisplayName("el Excel anual saca los doce meses y el total del anio")
    void anual() throws IOException {
        InformeAnualFilaDTO fila = new InformeAnualFilaDTO();
        fila.setDireccionCompleta("Calle del Sol 7, Bajo A, Getafe");
        fila.setInquilinos("Ana Diaz Moreno");
        fila.setDnis("56789012W");
        fila.acumular(1, new BigDecimal("540.00"));
        fila.acumular(2, new BigDecimal("540.00"));

        byte[] salida = new EscritorInformeAnualExcel().escribir(2026, List.of(fila));
        String contenido = volcarTexto(salida);

        assertThat(fila.getTotal()).isEqualByComparingTo("1080.00");
        assertThat(contenido).contains("Enero", "Diciembre", "Total anio", "Ana Diaz Moreno");
    }

    @Test
    @DisplayName("el Excel mensual incluye la fila de totales")
    void mensual() throws IOException {
        InformeMensualFilaDTO fila = new InformeMensualFilaDTO(
                "Calle Mayor 10, portal 2, 1 A", "Maria Lopez Garcia", "12345678Z",
                new BigDecimal("750.00"), new BigDecimal("750.00"), "05/01/2026", "FICHERO");

        String contenido = volcarTexto(new EscritorInformeMensualExcel().escribir(2026, 1, List.of(fila)));

        assertThat(contenido).contains("Rentas cobradas - Enero de 2026", "Maria Lopez Garcia", "TOTAL");
    }

    /** Concatena todo el texto del libro para poder afirmar sobre su contenido. */
    private static String volcarTexto(byte[] bytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook libro = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet hoja = libro.getSheetAt(0);
            hoja.forEach(fila -> fila.forEach(celda -> {
                sb.append(new org.apache.poi.ss.usermodel.DataFormatter().formatCellValue(celda)).append('\n');
            }));
        }
        return sb.toString();
    }
}
