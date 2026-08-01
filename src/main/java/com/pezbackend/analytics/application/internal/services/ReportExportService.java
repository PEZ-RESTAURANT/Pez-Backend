package com.pezbackend.analytics.application.internal.services;

import com.pezbackend.analytics.domain.model.valueobjects.ProductSalesInfo;
import com.pezbackend.analytics.domain.model.valueobjects.WaiterRankingInfo;
import com.pezbackend.analytics.domain.services.AnalyticsQueryService;
import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.valueobjects.SaleStatus;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de generar las exportaciones en formato PDF y Excel (xlsx) de los reportes.
 */
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final AnalyticsQueryService queryService;
    private final SaleRepository saleRepository;

    public byte[] exportToExcel(String report, LocalDate from, LocalDate to, Integer limit) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte");

            // Crear estilo de cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            if ("daily-revenue".equalsIgnoreCase(report)) {
                // Cabeceras
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Venta ID", "Mesa/Etiqueta", "Cliente", "Tipo Doc", "Total", "Fecha"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Cargar datos
                LocalDateTime start = from.atStartOfDay();
                LocalDateTime end = to.atTime(23, 59, 59);
                List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                        .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                        .collect(Collectors.toList());

                int rowNum = 1;
                for (Sale s : sales) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(s.getId());
                    row.createCell(1).setCellValue(s.getName());
                    row.createCell(2).setCellValue(s.getCustomerName());
                    row.createCell(3).setCellValue(s.getDocumentType().name());
                    row.createCell(4).setCellValue(s.getTotal().doubleValue());
                    row.createCell(5).setCellValue(s.getCreatedAt().toString());
                }
            } else if ("waiters-ranking".equalsIgnoreCase(report)) {
                // Cabeceras
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Posición", "Mozo ID", "Mozo", "Total Vendido"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                List<WaiterRankingInfo> ranking = queryService.getWaitersRanking(from, to);
                int rowNum = 1;
                for (int idx = 0; idx < ranking.size(); idx++) {
                    WaiterRankingInfo info = ranking.get(idx);
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(idx + 1);
                    row.createCell(1).setCellValue(info.waiterId());
                    row.createCell(2).setCellValue(info.firstName() + " " + info.lastName());
                    row.createCell(3).setCellValue(info.totalSales().doubleValue());
                }
            } else if ("top-products".equalsIgnoreCase(report) || "bottom-products".equalsIgnoreCase(report)) {
                // Cabeceras
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Posición", "Producto", "Cantidad Vendida", "Recaudación Total", "Alerta Ventas Bajas"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int maxLimit = limit != null ? limit : 10;
                List<ProductSalesInfo> products = "top-products".equalsIgnoreCase(report)
                        ? queryService.getTopProducts(from, to, maxLimit)
                        : queryService.getBottomProducts(from, to, maxLimit);

                int rowNum = 1;
                for (int idx = 0; idx < products.size(); idx++) {
                    ProductSalesInfo info = products.get(idx);
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(idx + 1);
                    row.createCell(1).setCellValue(info.productName());
                    row.createCell(2).setCellValue(info.quantitySold());
                    row.createCell(3).setCellValue(info.totalRevenue().doubleValue());
                    row.createCell(4).setCellValue(info.lowSalesAlert() ? "ALERTA" : "NORMAL");
                }
            }

            for (int col = 0; col < 6; col++) {
                sheet.autoSizeColumn(col);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al exportar reporte a Excel", e);
        }
    }

    public byte[] exportToPdf(String report, LocalDate from, LocalDate to, Integer limit) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Configurar fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("Reporte: " + report.toUpperCase(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Período: " + from.toString() + " al " + to.toString() + "\nGenerado: " + LocalDateTime.now(), subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));

            if ("daily-revenue".equalsIgnoreCase(report)) {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);

                String[] headers = {"Mesa/Etiqueta", "Cliente", "Tipo Doc", "Total", "Fecha"};
                for (String h : headers) {
                    table.addCell(new PdfPCell(new Phrase(h, boldCellFont)));
                }

                LocalDateTime start = from.atStartOfDay();
                LocalDateTime end = to.atTime(23, 59, 59);
                List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                        .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                        .collect(Collectors.toList());

                for (Sale s : sales) {
                    table.addCell(new Phrase(s.getName(), cellFont));
                    table.addCell(new Phrase(s.getCustomerName(), cellFont));
                    table.addCell(new Phrase(s.getDocumentType().name(), cellFont));
                    table.addCell(new Phrase(s.getTotal().toString(), cellFont));
                    table.addCell(new Phrase(s.getCreatedAt().toLocalDate().toString(), cellFont));
                }
                document.add(table);
            } else if ("waiters-ranking".equalsIgnoreCase(report)) {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);

                String[] headers = {"Posición", "Mozo ID", "Mozo", "Total Vendido"};
                for (String h : headers) {
                    table.addCell(new PdfPCell(new Phrase(h, boldCellFont)));
                }

                List<WaiterRankingInfo> ranking = queryService.getWaitersRanking(from, to);
                for (int idx = 0; idx < ranking.size(); idx++) {
                    WaiterRankingInfo info = ranking.get(idx);
                    table.addCell(new Phrase(String.valueOf(idx + 1), cellFont));
                    table.addCell(new Phrase(info.waiterId().toString(), cellFont));
                    table.addCell(new Phrase(info.firstName() + " " + info.lastName(), cellFont));
                    table.addCell(new Phrase(info.totalSales().toString(), cellFont));
                }
                document.add(table);
            } else if ("top-products".equalsIgnoreCase(report) || "bottom-products".equalsIgnoreCase(report)) {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);

                String[] headers = {"Posición", "Producto", "Cantidad", "Recaudación", "Alerta"};
                for (String h : headers) {
                    table.addCell(new PdfPCell(new Phrase(h, boldCellFont)));
                }

                int maxLimit = limit != null ? limit : 10;
                List<ProductSalesInfo> products = "top-products".equalsIgnoreCase(report)
                        ? queryService.getTopProducts(from, to, maxLimit)
                        : queryService.getBottomProducts(from, to, maxLimit);

                for (int idx = 0; idx < products.size(); idx++) {
                    ProductSalesInfo info = products.get(idx);
                    table.addCell(new Phrase(String.valueOf(idx + 1), cellFont));
                    table.addCell(new Phrase(info.productName(), cellFont));
                    table.addCell(new Phrase(String.valueOf(info.quantitySold()), cellFont));
                    table.addCell(new Phrase(info.totalRevenue().toString(), cellFont));
                    table.addCell(new Phrase(info.lowSalesAlert() ? "SÍ" : "NO", cellFont));
                }
                document.add(table);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al exportar reporte a PDF", e);
        }
    }
}
