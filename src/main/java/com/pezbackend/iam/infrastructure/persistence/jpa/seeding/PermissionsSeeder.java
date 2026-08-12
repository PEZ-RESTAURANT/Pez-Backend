package com.pezbackend.iam.infrastructure.persistence.jpa.seeding;

import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.entities.RolePermissionDefault;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PermissionRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RolePermissionDefaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sembrador de datos (seeder) encargado de inicializar el catálogo de permisos granulares
 * y sus configuraciones por defecto para los roles del sistema en el arranque de la aplicación.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionsSeeder {

    private final PermissionRepository permissionRepository;
    private final RolePermissionDefaultRepository defaultRepository;

    private record PermissionSeed(
            String code,
            String module,
            String description,
            boolean admin,
            boolean cashier,
            boolean waiter,
            boolean cook
    ) {}

    private static final List<PermissionSeed> SEEDS = List.of(
            // --- Orders ---
            new PermissionSeed("orders.create", "orders", "Crear pedidos en el sistema", true, true, true, false),
            new PermissionSeed("orders.view_table_map", "orders", "Ver mapa de mesas y estado del salón", true, true, true, false),
            new PermissionSeed("orders.change_table_status", "orders", "Cambiar el estado de ocupación de las mesas", true, true, true, false),
            new PermissionSeed("orders.modify_item", "orders", "Modificar ítems de un pedido activo", true, true, true, false),
            new PermissionSeed("orders.cancel_item", "orders", "Cancelar ítems de un pedido antes de ser preparados", true, true, false, false),
            new PermissionSeed("orders.delete_item", "orders", "Eliminar ítems de la cuenta de un pedido", true, true, false, false),
            new PermissionSeed("orders.adjust_price", "orders", "Aplicar descuentos o ajustes manuales al precio", true, false, false, false),
            new PermissionSeed("orders.issue_receipt", "orders", "Emitir o reemitir precuenta y comprobante", true, true, false, false),
            new PermissionSeed("orders.edit_layout", "orders", "Gestionar la distribución y diseño de mesas del local", true, false, false, false),

            // --- Catalog ---
            new PermissionSeed("catalog.edit_products_categories", "catalog", "Gestionar categorías y catálogo de productos", true, false, false, false),
            new PermissionSeed("catalog.edit_supplies_recipes", "catalog", "Gestionar insumos y recetas de cocina", true, false, false, false),
            new PermissionSeed("catalog.edit_kitchen_zones", "catalog", "Gestionar zonas de preparación de la cocina", true, false, false, false),
            new PermissionSeed("catalog.edit_layout", "catalog", "Gestionar la distribución de mesas del local", true, false, false, false),
            new PermissionSeed("catalog.edit_payment_methods", "catalog", "Gestionar métodos de pago admitidos", true, false, false, false),
            new PermissionSeed("catalog.edit_reasons", "catalog", "Gestionar motivos parametrizados de cancelaciones", true, false, false, false),
            new PermissionSeed("catalog.edit_fixed_expenses", "catalog", "Gestionar el catálogo de gastos fijos", true, false, false, false),
            new PermissionSeed("catalog.edit_sanctions", "catalog", "Gestionar catálogo de sanciones al personal", true, false, false, false),
            new PermissionSeed("catalog.edit_schedules_thresholds", "catalog", "Gestionar horarios de turnos y tolerancias", true, false, false, false),
            new PermissionSeed("catalog.edit_survey", "catalog", "Gestionar encuestas de satisfacción de clientes", true, false, false, false),

            // --- Cash Register ---
            new PermissionSeed("cashregister.view", "cashregister", "Visualizar flujo de caja y movimientos", true, true, false, false),
            new PermissionSeed("cashregister.open_close_shift", "cashregister", "Apertura y cierre de turnos de caja", true, true, false, false),
            new PermissionSeed("cashregister.register_movement", "cashregister", "Registrar entradas/salidas manuales de efectivo", true, true, false, false),
            new PermissionSeed("cashregister.register_payment", "cashregister", "Registrar cobros de comandas", true, true, false, false),
            new PermissionSeed("cashregister.view_mismatch", "cashregister", "Visualizar descuadres o diferencias de arqueo", true, true, false, false),

            // --- Inventory ---
            new PermissionSeed("inventory.view", "inventory", "Visualizar stock actual de almacén e insumos", true, false, false, false),
            new PermissionSeed("inventory.adjust_manual", "inventory", "Realizar ajustes manuales de inventario (merma/daños)", true, false, false, false),
            new PermissionSeed("inventory.restock", "inventory", "Registrar ingresos de mercadería por compras", true, false, false, false),

            // --- Kitchen ---
            new PermissionSeed("kitchen.view_own_zone", "kitchen", "Ver monitor de pedidos de la zona asignada", true, false, false, true),
            new PermissionSeed("kitchen.change_item_status", "kitchen", "Cambiar estado de platos (en preparación, listo)", true, false, false, true),
            new PermissionSeed("kitchen.view_zones", "kitchen", "Visualizar el catálogo de zonas de cocina", true, false, true, true),

            // --- Staff ---
            new PermissionSeed("staff.view", "staff", "Ver listado de personal y contratos", true, true, false, false),
            new PermissionSeed("staff.register_manual_attendance", "staff", "Registrar marcas de asistencia manuales", true, false, false, false),
            new PermissionSeed("staff.register_advance", "staff", "Registrar adelantos de sueldo a empleados", true, true, false, false),
            new PermissionSeed("staff.register_sanction", "staff", "Registrar sanciones o amonestaciones de empleados", true, false, false, false),
            new PermissionSeed("staff.register_overtime", "staff", "Registrar horas extras autorizadas", true, false, false, false),
            new PermissionSeed("staff.edit_profile", "staff", "Gestionar perfiles de empleados de la plantilla", true, false, false, false),
            new PermissionSeed("staff.manage_employees", "staff", "Gestionar perfiles, huellas y consultas del personal", true, false, false, false),
            new PermissionSeed("staff.register_attendance", "staff", "Registrar marca de asistencia", true, true, true, true),

            // --- Loyalty ---
            new PermissionSeed("loyalty.register_customer", "loyalty", "Registrar nuevos clientes en el programa de fidelización", true, true, false, false),
            new PermissionSeed("loyalty.view_analytics", "loyalty", "Visualizar métricas de consumo de clientes frecuentes", true, false, false, false),
            new PermissionSeed("loyalty.view", "loyalty", "Visualizar perfiles de clientes y su historial de puntos", true, true, false, false),
            new PermissionSeed("loyalty.redeem_points", "loyalty", "Registrar canjes de puntos de clientes", true, true, false, false),
            new PermissionSeed("loyalty.manage_config", "loyalty", "Gestionar la configuración del programa de fidelización", true, false, false, false),

            // --- Analytics & Shared Infrastructure ---
            new PermissionSeed("analytics.view", "analytics", "Visualizar métricas generales de venta e indicadores", true, false, false, false),
            new PermissionSeed("analytics.export", "analytics", "Exportar reportes de negocio a PDF o Excel", true, false, false, false),
            new PermissionSeed("analytics.manage_config", "analytics", "Gestionar la configuración del módulo de analítica", true, false, false, false),
            new PermissionSeed("audit.view", "audit", "Visualizar historial y traza de eventos de auditoría", true, false, false, false),
            new PermissionSeed("permissions.manage", "permissions", "Administrar permisos y overrides de usuarios", true, false, false, false),
            new PermissionSeed("iam.manage_accounts", "iam", "Administrar cuentas de usuario del personal", true, false, false, false),

            // --- Fase 10 Extensions ---
            new PermissionSeed("orders.merge_tables", "orders", "Unir o fusionar mesas del salón", true, true, false, false),
            new PermissionSeed("orders.transfer_order", "orders", "Trasladar pedido activo a otra mesa", true, true, true, false),
            new PermissionSeed("reservations.manage", "reservations", "Gestionar y operar reservas de mesas", true, true, false, false),
            new PermissionSeed("reservations.view", "reservations", "Visualizar reservas de mesas", true, true, true, false),
            new PermissionSeed("orders.force_unlock_table", "orders", "Forzar la liberación del bloqueo de una mesa", true, true, false, false)
    );

    /**
     * Inicializa los datos del catálogo tras levantarse el contexto de la aplicación.
     * <p>
     * Se ejecuta después del sembrado de roles iniciales (Order = 10 para asegurar orden).
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    @Transactional
    public void seed() {
        log.info("Sembrado de Permisos: Iniciando inicialización de catálogo...");
        int permissionsCreated = 0;
        int defaultsCreated = 0;

        for (PermissionSeed seed : SEEDS) {
            // 1. Obtener o crear el permiso
            Permission permission;
            if (!permissionRepository.existsByCode(seed.code())) {
                permission = new Permission(seed.code(), seed.module(), seed.description());
                permission = permissionRepository.save(permission);
                permissionsCreated++;
            } else {
                permission = permissionRepository.findByCode(seed.code()).orElseThrow();
            }

            // 2. Sembrar defaults por cada rol
            defaultsCreated += seedDefault(Roles.ADMIN, permission, seed.admin());
            defaultsCreated += seedDefault(Roles.CASHIER, permission, seed.cashier());
            defaultsCreated += seedDefault(Roles.WAITER, permission, seed.waiter());
            defaultsCreated += seedDefault(Roles.COOK, permission, seed.cook());
        }

        log.info("Sembrado de Permisos completado. Permisos nuevos: {}, Reglas de rol nuevas: {}",
                permissionsCreated, defaultsCreated);
    }

    private int seedDefault(Roles role, Permission permission, boolean granted) {
        if (!defaultRepository.findByRoleAndPermissionId(role, permission.getId()).isPresent()) {
            RolePermissionDefault rule = new RolePermissionDefault(role, permission, granted);
            defaultRepository.save(rule);
            return 1;
        }
        return 0;
    }
}
