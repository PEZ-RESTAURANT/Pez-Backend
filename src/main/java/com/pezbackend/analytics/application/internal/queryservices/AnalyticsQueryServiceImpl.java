package com.pezbackend.analytics.application.internal.queryservices;

import com.pezbackend.analytics.domain.model.entities.AnalyticsConfig;
import com.pezbackend.analytics.domain.model.valueobjects.*;
import com.pezbackend.analytics.domain.services.AnalyticsQueryService;
import com.pezbackend.analytics.infrastructure.persistence.jpa.repositories.AnalyticsConfigRepository;
import com.pezbackend.billing.domain.model.aggregates.Sale;
import com.pezbackend.billing.domain.model.entities.SaleDetail;
import com.pezbackend.billing.domain.model.valueobjects.SaleStatus;
import com.pezbackend.billing.infrastructure.persistence.jpa.repositories.SaleRepository;
import com.pezbackend.orders.domain.model.aggregates.Order;
import com.pezbackend.orders.domain.model.entities.OrderItem;
import com.pezbackend.orders.domain.model.valueobjects.OrderItemStatus;
import com.pezbackend.orders.infrastructure.persistence.jpa.repositories.OrderRepository;
import com.pezbackend.cashregister.domain.model.entities.CashMovement;
import com.pezbackend.cashregister.domain.model.valueobjects.CashMovementType;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.catalog.domain.model.aggregates.Product;
import com.pezbackend.catalog.domain.model.entities.ProductKitchenZone;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.pezbackend.catalog.infrastructure.persistence.jpa.repositories.ProductKitchenZoneRepository;
import com.pezbackend.kitchen.domain.model.entities.KitchenZone;
import com.pezbackend.kitchen.infrastructure.persistence.jpa.repositories.KitchenZoneRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de AnalyticsQueryService encargada de resolver las consultas analíticas del negocio.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final AnalyticsConfigRepository configRepository;
    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductKitchenZoneRepository productKitchenZoneRepository;
    private final KitchenZoneRepository kitchenZoneRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BigDecimal getDailyRevenue(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getMonthlyRevenue(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay.plusMonths(1).atStartOfDay().minusNanos(1);
        return saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public NetProfitInfo getNetProfit(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        // Ingresos
        BigDecimal totalRevenue = saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Egresos (EXPENSE) de caja registradora
        BigDecimal totalExpenses = entityManager.createQuery(
                "SELECT COALESCE(SUM(cm.amount), 0) FROM CashMovement cm WHERE cm.type = :type AND cm.createdAt >= :start AND cm.createdAt <= :end",
                BigDecimal.class)
                .setParameter("type", CashMovementType.EXPENSE)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        return new NetProfitInfo(totalRevenue, totalExpenses, netProfit, true);
    }

    @Override
    public BreakevenInfo getBreakeven(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        // Gastos fijos (total egresos en el período)
        BigDecimal fixedExpenses = entityManager.createQuery(
                "SELECT COALESCE(SUM(cm.amount), 0) FROM CashMovement cm WHERE cm.type = :type AND cm.createdAt >= :start AND cm.createdAt <= :end",
                BigDecimal.class)
                .setParameter("type", CashMovementType.EXPENSE)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        // Ventas pagadas
        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .toList();

        long salesCount = sales.size();
        BigDecimal totalRevenue = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTicket = salesCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(salesCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal breakevenSalesCount = averageTicket.compareTo(BigDecimal.ZERO) > 0
                ? fixedExpenses.divide(averageTicket, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new BreakevenInfo(fixedExpenses, averageTicket, breakevenSalesCount, true);
    }

    @Override
    public ComparisonInfo compare(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB) {
        NetProfitInfo infoA = getNetProfit(fromA, toA);
        NetProfitInfo infoB = getNetProfit(fromB, toB);

        long salesCountA = saleRepository.findByCreatedAtBetween(fromA.atStartOfDay(), toA.atTime(LocalTime.MAX)).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID).count();
        long salesCountB = saleRepository.findByCreatedAtBetween(fromB.atStartOfDay(), toB.atTime(LocalTime.MAX)).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID).count();

        BigDecimal ticketAverageA = salesCountA > 0 ? infoA.totalRevenue().divide(BigDecimal.valueOf(salesCountA), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal ticketAverageB = salesCountB > 0 ? infoB.totalRevenue().divide(BigDecimal.valueOf(salesCountB), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return new ComparisonInfo(
                infoA.totalRevenue(), infoB.totalRevenue(), infoB.totalRevenue().subtract(infoA.totalRevenue()),
                infoA.totalExpenses(), infoB.totalExpenses(), infoB.totalExpenses().subtract(infoA.totalExpenses()),
                infoA.netProfit(), infoB.netProfit(), infoB.netProfit().subtract(infoA.netProfit()),
                salesCountA, salesCountB, salesCountB - salesCountA,
                ticketAverageA, ticketAverageB, ticketAverageB.subtract(ticketAverageA)
        );
    }

    @Override
    public String getPresets() {
        return getConfig().getDatePresets();
    }

    @Override
    public List<KitchenZonePerformanceInfo> getKitchenZonePerformance(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .toList();

        Map<String, ZoneAccumulator> map = new HashMap<>();
        for (Sale s : sales) {
            for (SaleDetail d : s.getDetails()) {
                Product product = entityManager.createQuery(
                        "SELECT p FROM Product p WHERE p.name = :name", Product.class)
                        .setParameter("name", d.getProductName())
                        .getResultList().stream().findFirst().orElse(null);
                if (product == null) continue;

                ProductKitchenZone pkz = productKitchenZoneRepository.findByProductId(product.getId()).orElse(null);
                if (pkz == null) continue;

                KitchenZone zone = kitchenZoneRepository.findById(pkz.getZoneId()).orElse(null);
                if (zone == null) continue;

                ZoneAccumulator acc = map.computeIfAbsent(zone.getName(), k -> new ZoneAccumulator());
                acc.itemsCount += d.getQuantity();
                acc.revenue = acc.revenue.add(d.getTotalPrice());
            }
        }

        return map.entrySet().stream()
                .map(e -> new KitchenZonePerformanceInfo(e.getKey(), e.getValue().itemsCount, e.getValue().revenue))
                .sorted(Comparator.comparing(KitchenZonePerformanceInfo::revenue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<WaiterRankingInfo> getWaitersRanking(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .toList();

        Map<Long, BigDecimal> waiterSalesMap = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getOrderId() == null) continue;
            Order order = orderRepository.findById(sale.getOrderId()).orElse(null);
            if (order == null) continue;

            for (OrderItem item : order.getItems()) {
                if (item.getWaiterId() == null) continue;
                BigDecimal itemTotal = item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
                waiterSalesMap.put(item.getWaiterId(), waiterSalesMap.getOrDefault(item.getWaiterId(), BigDecimal.ZERO).add(itemTotal));
            }
        }

        List<WaiterRankingInfo> ranking = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : waiterSalesMap.entrySet()) {
            User waiter = userRepository.findById(entry.getKey()).orElse(null);
            String firstName = waiter != null ? waiter.getFirstName() : "Mozo";
            String lastName = waiter != null ? waiter.getLastName() : "ID #" + entry.getKey();
            ranking.add(new WaiterRankingInfo(entry.getKey(), firstName, lastName, entry.getValue()));
        }

        ranking.sort((a, b) -> b.totalSales().compareTo(a.totalSales()));
        return ranking;
    }

    @Override
    public List<ProductSalesInfo> getTopProducts(LocalDate from, LocalDate to, int limit) {
        return getProductSalesSorted(from, to, limit, true);
    }

    @Override
    public List<ProductSalesInfo> getBottomProducts(LocalDate from, LocalDate to, int limit) {
        return getProductSalesSorted(from, to, limit, false);
    }

    private List<ProductSalesInfo> getProductSalesSorted(LocalDate from, LocalDate to, int limit, boolean descending) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .toList();

        Map<String, ProductSalesAccumulator> productMap = new HashMap<>();

        for (Sale sale : sales) {
            for (SaleDetail detail : sale.getDetails()) {
                ProductSalesAccumulator acc = productMap.computeIfAbsent(detail.getProductName(), k -> new ProductSalesAccumulator());
                acc.quantitySold += detail.getQuantity();
                acc.totalRevenue = acc.totalRevenue.add(detail.getTotalPrice());
            }
        }

        AnalyticsConfig config = getConfig();
        LocalDateTime evaluationStart = LocalDateTime.now().minusDays(config.getLowSalesEvaluationPeriodDays());
        List<Sale> evaluationSales = saleRepository.findByCreatedAtBetween(evaluationStart, LocalDateTime.now()).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .toList();

        Map<String, Long> evaluationQtyMap = new HashMap<>();
        for (Sale es : evaluationSales) {
            for (SaleDetail ed : es.getDetails()) {
                evaluationQtyMap.put(ed.getProductName(), evaluationQtyMap.getOrDefault(ed.getProductName(), 0L) + ed.getQuantity());
            }
        }

        List<ProductSalesInfo> products = new ArrayList<>();
        for (Map.Entry<String, ProductSalesAccumulator> entry : productMap.entrySet()) {
            long totalEvalQty = evaluationQtyMap.getOrDefault(entry.getKey(), 0L);
            boolean lowSalesAlert = totalEvalQty < config.getLowSalesThresholdUnits();
            products.add(new ProductSalesInfo(entry.getKey(), entry.getValue().quantitySold, entry.getValue().totalRevenue, lowSalesAlert));
        }

        if (descending) {
            products.sort(Comparator.comparing(ProductSalesInfo::quantitySold).reversed());
        } else {
            products.sort(Comparator.comparing(ProductSalesInfo::quantitySold));
        }

        return products.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<DailyProductionInfo> getDailyProduction(Long productId, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<OrderItem> items = entityManager.createQuery(
                "SELECT oi FROM OrderItem oi WHERE oi.productId = :productId AND oi.status IN (:statuses) AND oi.readyAt >= :start AND oi.readyAt <= :end",
                OrderItem.class)
                .setParameter("productId", productId)
                .setParameter("statuses", List.of(OrderItemStatus.READY, OrderItemStatus.DELIVERED))
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<LocalDate, Integer> dailyMap = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getReadyAt().toLocalDate(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        List<DailyProductionInfo> list = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            list.add(new DailyProductionInfo(date, dailyMap.getOrDefault(date, 0)));
        }
        return list;
    }

    @Override
    public List<ComboInfo> getTopCombos(LocalDate from, LocalDate to, int limit) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end).stream()
                .filter(s -> s.getSaleStatus() == SaleStatus.PAID)
                .toList();

        Map<ProductPair, Long> comboMap = new HashMap<>();

        for (Sale sale : sales) {
            List<String> productNames = sale.getDetails().stream()
                    .map(SaleDetail::getProductName)
                    .distinct()
                    .toList();

            for (int i = 0; i < productNames.size(); i++) {
                for (int j = i + 1; j < productNames.size(); j++) {
                    ProductPair pair = new ProductPair(productNames.get(i), productNames.get(j));
                    comboMap.put(pair, comboMap.getOrDefault(pair, 0L) + 1);
                }
            }
        }

        return comboMap.entrySet().stream()
                .map(e -> new ComboInfo(e.getKey().productA(), e.getKey().productB(), e.getValue()))
                .sorted(Comparator.comparing(ComboInfo::count).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public AnalyticsConfig getConfig() {
        return configRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> new AnalyticsConfig(5, 30, "{}"));
    }

    // Clases auxiliares acumuladoras
    private static class ZoneAccumulator {
        long itemsCount = 0;
        BigDecimal revenue = BigDecimal.ZERO;
    }

    private static class ProductSalesAccumulator {
        long quantitySold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
    }

    private record ProductPair(String productA, String productB) {
        public ProductPair {
            if (productA.compareTo(productB) > 0) {
                String temp = productA;
                productA = productB;
                productB = temp;
            }
        }
    }
}
