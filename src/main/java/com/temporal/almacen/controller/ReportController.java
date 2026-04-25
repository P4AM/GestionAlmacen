package com.temporal.almacen.controller;

import com.temporal.almacen.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/excel/inventario")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] data = reportService.generarExcelInventario();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/excel/ventas")
    public ResponseEntity<byte[]> exportVentasExcel() throws IOException {
        byte[] data = reportService.generarExcelVentas();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/pdf/stock-critico")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] data = reportService.generarPdfStockCritico();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock_critico.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
