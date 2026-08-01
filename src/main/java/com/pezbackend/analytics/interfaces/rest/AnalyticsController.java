package com.pezbackend.analytics.interfaces.rest;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;
import com.pezbackend.analytics.domain.model.valueobjects.*;
import com.pezbackend.analytics.domain.services.AnalyticsCommandService;
import com.pezbackend.analytics.domain.services.AnalyticsQueryService;
import com.pezbackend.analytics.application.internal.services.ReportExportService;
import com.pezbackend.analytics.interfaces.rest.resources.AnalyticsConfigResource;
import com.pezbackend.analytics.interfaces.rest.resources.DailyRevenueResource;
import com.pezbackend.analytics.interfaces.rest.resources.MonthlyRevenueResource;
import com.pezbackend.iam.infrastructure.authorization.sfs.annotations.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para el módulo de analítica y reportes de negocio.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsQueryService queryService;
    private final AnalyticsCommandService commandService;
    private final ReportExportService exportService;

    @GetMapping("/revenue/daily")
    @RequiresPermission("analytics.view")
    public ResponseEntity<DailyRevenueResource> getDailyRevenue(@RequestParam(required = false) String date) {
        LocalDate localDate = parseDate(date, LocalDate.now());
        BigDecimal total = queryService.getDailyRevenue(localDate);
        return ResponseEntity.ok(new DailyRevenueResource(total));
    }

    @GetMapping("/revenue/monthly")
    @RequiresPermission("analytics.view")
    public ResponseEntity<MonthlyRevenueResource> getMonthlyRevenue(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        BigDecimal total = queryService.getMonthlyRevenue(y, m);
        return ResponseEntity.ok(new MonthlyRevenueResource(total));
    }

    @GetMapping("/net-profit")
    @RequiresPermission("analytics.view")
    public ResponseEntity<NetProfitInfo> getNetProfit(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getNetProfit(startDate, endDate));
    }

    @GetMapping("/breakeven")
    @RequiresPermission("analytics.view")
    public ResponseEntity<BreakevenInfo> getBreakeven(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getBreakeven(startDate, endDate));
    }

    @GetMapping("/compare")
    @RequiresPermission("analytics.view")
    public ResponseEntity<ComparisonInfo> compare(
            @RequestParam String fromA,
            @RequestParam String toA,
            @RequestParam String fromB,
            @RequestParam String toB) {
        LocalDate startA = LocalDate.parse(fromA);
        LocalDate endA = LocalDate.parse(toA);
        LocalDate startB = LocalDate.parse(fromB);
        LocalDate endB = LocalDate.parse(toB);
        return ResponseEntity.ok(queryService.compare(startA, endA, startB, endB));
    }

    @GetMapping("/presets")
    @RequiresPermission("analytics.view")
    public ResponseEntity<String> getPresets() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(queryService.getPresets());
    }

    @GetMapping("/kitchen-zone-performance")
    @RequiresPermission("analytics.view")
    public ResponseEntity<List<KitchenZonePerformanceInfo>> getKitchenZonePerformance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getKitchenZonePerformance(startDate, endDate));
    }

    @GetMapping("/waiters/ranking")
    @RequiresPermission("analytics.view")
    public ResponseEntity<List<WaiterRankingInfo>> getWaitersRanking(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getWaitersRanking(startDate, endDate));
    }

    @GetMapping("/products/top")
    @RequiresPermission("analytics.view")
    public ResponseEntity<List<ProductSalesInfo>> getTopProducts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getTopProducts(startDate, endDate, limit));
    }

    @GetMapping("/products/bottom")
    @RequiresPermission("analytics.view")
    public ResponseEntity<List<ProductSalesInfo>> getBottomProducts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getBottomProducts(startDate, endDate, limit));
    }

    @GetMapping("/products/{id}/daily-production")
    @RequiresPermission("analytics.view")
    public ResponseEntity<List<DailyProductionInfo>> getDailyProduction(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getDailyProduction(id, startDate, endDate));
    }

    @GetMapping("/combos/top")
    @RequiresPermission("analytics.view")
    public ResponseEntity<List<ComboInfo>> getTopCombos(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());
        return ResponseEntity.ok(queryService.getTopCombos(startDate, endDate, limit));
    }

    @GetMapping("/export")
    @RequiresPermission("analytics.export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String report,
            @RequestParam String format,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit) {

        LocalDate startDate = parseDate(from, LocalDate.now().minusDays(30));
        LocalDate endDate = parseDate(to, LocalDate.now());

        byte[] data;
        MediaType mediaType;
        String extension;

        if ("pdf".equalsIgnoreCase(format)) {
            data = exportService.exportToPdf(report, startDate, endDate, limit);
            mediaType = MediaType.APPLICATION_PDF;
            extension = "pdf";
        } else {
            data = exportService.exportToExcel(report, startDate, endDate, limit);
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            extension = "xlsx";
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + report + "." + extension)
                .body(data);
    }

    @GetMapping("/config")
    @RequiresPermission("analytics.manage_config")
    public ResponseEntity<AnalyticsConfigResource> getConfig() {
        AnalyticsConfig config = queryService.getConfig();
        return ResponseEntity.ok(new AnalyticsConfigResource(
                config.getLowSalesThresholdUnits(),
                config.getLowSalesEvaluationPeriodDays(),
                config.getDatePresets()
        ));
    }

    @PutMapping("/config")
    @RequiresPermission("analytics.manage_config")
    public ResponseEntity<AnalyticsConfigResource> updateConfig(@RequestBody AnalyticsConfigResource resource) {
        AnalyticsConfig config = commandService.updateConfig(
                resource.lowSalesThresholdUnits(),
                resource.lowSalesEvaluationPeriodDays(),
                resource.datePresets()
        );
        return ResponseEntity.ok(new AnalyticsConfigResource(
                config.getLowSalesThresholdUnits(),
                config.getLowSalesEvaluationPeriodDays(),
                config.getDatePresets()
        ));
    }

    private LocalDate parseDate(String dateStr, LocalDate defaultDate) {
        if (dateStr == null || dateStr.isBlank()) {
            return defaultDate;
        }
        return LocalDate.parse(dateStr);
    }
}
