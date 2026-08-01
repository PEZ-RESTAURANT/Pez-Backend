package com.pezbackend.analytics.domain.services;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;
import com.pezbackend.analytics.domain.model.valueobjects.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz para las operaciones de consulta del módulo de analítica.
 */
public interface AnalyticsQueryService {

    BigDecimal getDailyRevenue(LocalDate date);

    BigDecimal getMonthlyRevenue(int year, int month);

    NetProfitInfo getNetProfit(LocalDate from, LocalDate to);

    BreakevenInfo getBreakeven(LocalDate from, LocalDate to);

    ComparisonInfo compare(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB);

    String getPresets();

    List<KitchenZonePerformanceInfo> getKitchenZonePerformance(LocalDate from, LocalDate to);

    List<WaiterRankingInfo> getWaitersRanking(LocalDate from, LocalDate to);

    List<ProductSalesInfo> getTopProducts(LocalDate from, LocalDate to, int limit);

    List<ProductSalesInfo> getBottomProducts(LocalDate from, LocalDate to, int limit);

    List<DailyProductionInfo> getDailyProduction(Long productId, LocalDate from, LocalDate to);

    List<ComboInfo> getTopCombos(LocalDate from, LocalDate to, int limit);

    AnalyticsConfig getConfig();
}
