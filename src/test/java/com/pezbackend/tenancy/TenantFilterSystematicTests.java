package com.pezbackend.tenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantFilterSystematicTests {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<String> ENTITY_NAMES = List.of(
            "OperationalConfig", "OvertimeRecord", "Sanction", "PayrollAdjustment",
            "AttendanceRecord", "StaffProfile", "AuditEvent", "OrderItem",
            "Reservation", "RestaurantTable", "PriceAdjustment", "Order",
            "Customer", "SatisfactionSurvey", "PointsTransaction", "LoyaltyConfig",
            "KitchenZone", "Supply", "StockMovement", "Category",
            "User", "Product", "CashRegister", "Sale",
            "PaymentMethodConfig", "CashMovement", "CashRegisterMismatch", "AnalyticsConfig"
    );

    private void insertRawRow(String tableName, Long tenantId, int index) {
        if ("audit_events".equals(tableName)) {
            com.pezbackend.shared.domain.model.AuditEvent event = new com.pezbackend.shared.domain.model.AuditEvent(
                    "LOGIN", "IAM", "1", "device", Map.of("key", "value"), "reason", java.time.LocalDateTime.now()
            );
            event.setRestaurantId(tenantId);
            entityManager.persist(event);
            return;
        }

        String query;
        switch (tableName) {
            case "operational_configs":
                query = "INSERT INTO operational_configs (restaurant_id, created_at, updated_at, cutoff_hour, cutoff_minute, unattended_threshold_minutes, waiting_dishes_threshold_minutes) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 0, 15, 30)";
                break;
            case "overtime_records":
                query = "INSERT INTO overtime_records (restaurant_id, created_at, updated_at, staff_profile_id, hours, date, registered_by) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + tenantId + ", 2.0, '2026-08-11', 'admin@test.com')";
                break;
            case "sanctions":
                query = "INSERT INTO sanctions (restaurant_id, created_at, updated_at, staff_profile_id, type, reason, registered_by, date) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + tenantId + ", 'MISCONDUCT', 'Reason', 'admin@test.com', '2026-08-11')";
                break;
            case "payroll_adjustments":
                query = "INSERT INTO payroll_adjustments (restaurant_id, created_at, updated_at, staff_profile_id, type, amount, registered_by, date) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + tenantId + ", 'ADVANCE', 10.0, 'admin@test.com', '2026-08-11')";
                break;
            case "attendance_records":
                query = "INSERT INTO attendance_records (restaurant_id, created_at, updated_at, staff_profile_id, check_in_at, method) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + tenantId + ", CURRENT_TIMESTAMP, 'MANUAL_BY_ADMIN')";
                break;
            case "staff_profiles":
                query = "INSERT INTO staff_profiles (id, restaurant_id, created_at, updated_at, payment_type, agreed_amount, account_id) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'DAILY', 100.0, " + tenantId + ")";
                break;
            case "order_items":
                query = "INSERT INTO order_items (restaurant_id, created_at, order_id, product_id, quantity, waiter_id, unit_price_snapshot, status) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, " + tenantId + ", " + tenantId + ", 1, 1, 10.0, 'PENDING')";
                break;
            case "reservations":
                query = "INSERT INTO reservations (restaurant_id, created_at, updated_at, customer_name, customer_phone, party_size, reservation_date_time, status) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Customer', '123456', 2, CURRENT_TIMESTAMP, 'CONFIRMED')";
                break;
            case "restaurant_tables":
                query = "INSERT INTO restaurant_tables (restaurant_id, number, floor, position_x, position_y, status) " +
                        "VALUES (" + tenantId + ", " + (tenantId * 10 + index) + ", 1, 0, 0, 'FREE')";
                break;
            case "price_adjustments":
                query = "INSERT INTO price_adjustments (restaurant_id, order_id, scope, validity, new_value, applied_by, reason) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", 'INDIVIDUAL', 'TEMPORARY', 10.0, 'admin', 'reason')";
                break;
            case "orders":
                query = "INSERT INTO orders (id, restaurant_id, created_at, type, status) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", CURRENT_TIMESTAMP, 'DINE_IN', 'UNATTENDED')";
                break;
            case "customers":
                query = "INSERT INTO customers (id, restaurant_id, created_at, updated_at, full_name, phone, points_balance, data_consent_accepted) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Customer', '" + tenantId + "_" + index + "', 0, true)";
                break;
            case "satisfaction_surveys":
                query = "INSERT INTO satisfaction_surveys (restaurant_id, created_at, updated_at, customer_id, service_satisfaction, food_satisfaction, date) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + tenantId + ", 5, 5, '2026-08-11')";
                break;
            case "points_transactions":
                query = "INSERT INTO points_transactions (restaurant_id, created_at, updated_at, customer_id, amount, type, date) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + tenantId + ", 10, 'EARNED', '2026-08-11')";
                break;
            case "loyalty_configs":
                query = "INSERT INTO loyalty_configs (restaurant_id, created_at, updated_at, min_purchase_amount_for_points, points_per_currency_unit, review_satisfaction_threshold) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10.0, 1.0, 4)";
                break;
            case "kitchen_zones":
                query = "INSERT INTO kitchen_zones (id, restaurant_id, name) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", 'Zone " + tenantId + "_" + index + "')";
                break;
            case "supplies":
                query = "INSERT INTO supplies (id, restaurant_id, name, unit, current_stock, min_threshold) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", 'Supply " + tenantId + "_" + index + "', 'KG', 10.0, 2.0)";
                break;
            case "stock_movements":
                query = "INSERT INTO stock_movements (restaurant_id, supply_id, quantity, type, reason, registered_by, date) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", 5.0, 'RESTOCK', 'Restocking', 'admin@test.com', CURRENT_TIMESTAMP)";
                break;
            case "categories":
                query = "INSERT INTO categories (id, restaurant_id, created_at, updated_at, name) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Category " + tenantId + "_" + index + "')";
                break;
            case "user":
                query = "INSERT INTO user (restaurant_id, created_at, updated_at, email, password_hash, first_name, last_name, is_verified, active) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'user_" + tenantId + "_" + index + "@test.com', 'hash', 'First', 'Last', true, true)";
                break;
            case "product":
                query = "INSERT INTO product (id, restaurant_id, created_at, updated_at, name, price, category_id) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Product " + tenantId + "_" + index + "', 10.0, " + tenantId + ")";
                break;
            case "cash_register":
                query = "INSERT INTO cash_register (id, restaurant_id, created_at, updated_at, opening_balance, current_balance, status) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100.0, 100.0, 'OPEN')";
                break;
            case "sale":
                query = "INSERT INTO sale (restaurant_id, created_at, updated_at, document_type, total) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'BOLETA', 10.0)";
                break;
            case "payment_method_configs":
                query = "INSERT INTO payment_method_configs (restaurant_id, created_at, updated_at, name, payment_type, active) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Cash " + tenantId + "_" + index + "', 'CASH', true)";
                break;
            case "cash_movement":
                query = "INSERT INTO cash_movement (restaurant_id, created_at, updated_at, amount, type) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 10.0, 'INCOME')";
                break;
            case "cash_register_mismatches":
                query = "INSERT INTO cash_register_mismatches (restaurant_id, cash_register_id, expected_amount, declared_amount, status) " +
                        "VALUES (" + tenantId + ", " + tenantId + ", 100.0, 90.0, 'MISMATCHED')";
                break;
            case "analytics_configs":
                query = "INSERT INTO analytics_configs (restaurant_id, created_at, updated_at, low_sales_threshold_units, low_sales_evaluation_period_days, date_presets) " +
                        "VALUES (" + tenantId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5, 30, '{}')";
                break;
            default:
                throw new IllegalArgumentException("Unknown table name for entity test: " + tableName);
        }

        entityManager.createNativeQuery(query).executeUpdate();
    }

    private String getTableName(String entityName) {
        switch (entityName) {
            case "OperationalConfig": return "operational_configs";
            case "OvertimeRecord": return "overtime_records";
            case "Sanction": return "sanctions";
            case "PayrollAdjustment": return "payroll_adjustments";
            case "AttendanceRecord": return "attendance_records";
            case "StaffProfile": return "staff_profiles";
            case "AuditEvent": return "audit_events";
            case "OrderItem": return "order_items";
            case "Reservation": return "reservations";
            case "RestaurantTable": return "restaurant_tables";
            case "PriceAdjustment": return "price_adjustments";
            case "Order": return "orders";
            case "Customer": return "customers";
            case "SatisfactionSurvey": return "satisfaction_surveys";
            case "PointsTransaction": return "points_transactions";
            case "LoyaltyConfig": return "loyalty_configs";
            case "KitchenZone": return "kitchen_zones";
            case "Supply": return "supplies";
            case "StockMovement": return "stock_movements";
            case "Category": return "categories";
            case "User": return "user";
            case "Product": return "product";
            case "CashRegister": return "cash_register";
            case "Sale": return "sale";
            case "PaymentMethodConfig": return "payment_method_configs";
            case "CashMovement": return "cash_movement";
            case "CashRegisterMismatch": return "cash_register_mismatches";
            case "AnalyticsConfig": return "analytics_configs";
            default: throw new IllegalArgumentException("Unknown entity name: " + entityName);
        }
    }

    @Test
    public void testSystematicTenantFiltersOnAllEntities() {
        // Disable referential integrity to insert raw rows easily
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        // 1. Seed two dummy restaurants to avoid FK issues with restaurant_id
        entityManager.createNativeQuery(
                "INSERT INTO restaurants (id, name, active, created_at, updated_at) " +
                "VALUES (99901, 'Test Restaurant 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
                ).executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO restaurants (id, name, active, created_at, updated_at) " +
                "VALUES (99902, 'Test Restaurant 2', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
                ).executeUpdate();

        // 2. Loop over each entity table, insert one row for Tenant 99901 and one for Tenant 99902
        for (String entityName : ENTITY_NAMES) {
            String tableName = getTableName(entityName);
            insertRawRow(tableName, 99901L, 1);
            insertRawRow(tableName, 99902L, 2);
        }

        // 3. Enable Hibernate Filter for Tenant 99901 and check that only 1 record is visible
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("restaurantId", 99901L);

        for (String entityName : ENTITY_NAMES) {
            // Query using JPA/HQL (which automatically applies Hibernate Filters)
            List<?> results = entityManager.createQuery("FROM " + entityName).getResultList();

            // Assert that ONLY 1 record is visible (the one belonging to tenant 99901)
            assertEquals(1, results.size(), 
                    "Fuga de datos detectada: El filtro de tenant no limitó correctamente los resultados para la entidad " + entityName);
        }

        // 4. Change Hibernate Filter to Tenant 99902 and verify again
        session.enableFilter("tenantFilter").setParameter("restaurantId", 99902L);

        for (String entityName : ENTITY_NAMES) {
            List<?> results = entityManager.createQuery("FROM " + entityName).getResultList();

            assertEquals(1, results.size(), 
                    "Fuga de datos detectada: El filtro de tenant no limitó correctamente los resultados para la entidad " + entityName);
        }

        // Re-enable referential integrity
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
