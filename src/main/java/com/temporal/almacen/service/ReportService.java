package com.temporal.almacen.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.temporal.almacen.model.OrdenVenta;
import com.temporal.almacen.model.Producto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InventarioService inventarioService;
    private final VentasService ventasService;

    public byte[] generarExcelInventario() throws IOException {
        List<Producto> productos = inventarioService.listarProductos();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario");
            
            // Estilo de encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            headerStyle.setFont(font);

            // Encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Nombre", "Categoría", "Stock", "Precio", "Valor Total"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (Producto p : productos) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getCategoria().getNombre());
                row.createCell(3).setCellValue(p.getStock());
                row.createCell(4).setCellValue(p.getPrecio().doubleValue());
                row.createCell(5).setCellValue(p.getStock() * p.getPrecio().doubleValue());
            }

            // Auto-ajustar columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generarExcelVentas() throws IOException {
        List<OrdenVenta> ventas = ventasService.listarOrdenes();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ventas");
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            headerStyle.setFont(font);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Orden", "Fecha", "Cliente", "Estado", "Total"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;
            for (OrdenVenta v : ventas) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("#" + v.getId());
                row.createCell(1).setCellValue(v.getFecha().format(formatter));
                row.createCell(2).setCellValue(v.getCliente() != null ? v.getCliente().getNombre() : "VENTA RÁPIDA");
                row.createCell(3).setCellValue(v.getEstado());
                row.createCell(4).setCellValue(v.getTotal());
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generarPdfStockCritico() {
        List<Producto> productosBajos = inventarioService.obtenerProductosBajos();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            
            document.open();
            
            // Título
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Paragraph title = new Paragraph("Reporte de Stock Crítico", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Fecha
            Paragraph date = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            date.setSpacingAfter(30);
            document.add(date);

            // Tabla
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            
            // Estilo tabla
            addTableHeader(table);
            
            for (Producto p : productosBajos) {
                table.addCell(p.getNombre());
                table.addCell(p.getCategoria().getNombre());
                table.addCell(String.valueOf(p.getStock()));
                table.addCell(p.getPrecio().toString());
            }

            document.add(table);
            document.close();
            
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"Producto", "Categoría", "Stock", "Precio"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(Color.GRAY);
            cell.setPadding(5);
            cell.setPhrase(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            table.addCell(cell);
        }
    }
}
