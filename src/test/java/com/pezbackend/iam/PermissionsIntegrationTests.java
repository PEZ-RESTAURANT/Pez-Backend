package com.pezbackend.iam;

import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Permission;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.OverrideValue;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.domain.services.PermissionCommandService;
import com.pezbackend.iam.domain.services.PermissionQueryService;
import com.pezbackend.iam.domain.services.PermissionResolutionService;
import com.pezbackend.iam.domain.model.commands.CreatePermissionOverrideCommand;
import com.pezbackend.iam.domain.model.commands.DeletePermissionOverrideCommand;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.PermissionRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.shared.domain.exceptions.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de integración para validar el catálogo de permisos, su sembrado,
 * la lógica de resolución (regla OR y overrides) y la protección del permiso especial permissions.manage.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PermissionsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PermissionResolutionService resolutionService;

    @Autowired
    private PermissionCommandService commandService;

    @Autowired
    private PermissionQueryService queryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private User testUser;
    private Permission testPermission;

    @BeforeEach
    public void setUp() {
        // Asegurar la existencia del rol CASHIER en H2
        Role cashierRole = roleRepository.findByName(Roles.CASHIER)
                .orElseGet(() -> roleRepository.save(new Role(Roles.CASHIER)));

        // Crear un usuario de prueba asociado al rol CASHIER
        testUser = new User("cashier_test@pez.com", "hashed_password", "Test", "Cashier", true);
        testUser.addRole(cashierRole);
        testUser = userRepository.save(testUser);

        // Obtener el permiso de prueba
        testPermission = permissionRepository.findByCode("orders.create")
                .orElseGet(() -> permissionRepository.save(new Permission("orders.create", "orders", "Crear pedidos")));
    }

    @Test
    public void testRoleDefaultResolution() {
        // Por defecto, CASHIER tiene orders.create = true y catalog.edit_layout = false
        boolean hasOrdersCreate = resolutionService.hasPermission(testUser.getId(), "orders.create");
        assertThat(hasOrdersCreate).isTrue();

        boolean hasCatalogEdit = resolutionService.hasPermission(testUser.getId(), "catalog.edit_layout");
        assertThat(hasCatalogEdit).isFalse();
    }

    @Test
    public void testOverrideGranted() {
        // Inicialmente el cajero no tiene catalog.edit_layout
        boolean initial = resolutionService.hasPermission(testUser.getId(), "catalog.edit_layout");
        assertThat(initial).isFalse();

        // Aplicar override de GRANTED
        CreatePermissionOverrideCommand command = new CreatePermissionOverrideCommand(
                testUser.getId(),
                "catalog.edit_layout",
                OverrideValue.GRANTED,
                "Concesión de prueba para auditoría",
                "admin_user"
        );
        commandService.handle(command);

        // Verificar que ahora el permiso resuelto efectivo es true
        boolean resolved = resolutionService.hasPermission(testUser.getId(), "catalog.edit_layout");
        assertThat(resolved).isTrue();
    }

    @Test
    public void testOverrideRevoked() {
        // Inicialmente el cajero tiene orders.create
        boolean initial = resolutionService.hasPermission(testUser.getId(), "orders.create");
        assertThat(initial).isTrue();

        // Aplicar override de REVOKED
        CreatePermissionOverrideCommand command = new CreatePermissionOverrideCommand(
                testUser.getId(),
                "orders.create",
                OverrideValue.REVOKED,
                "Revocación de prueba para auditoría",
                "admin_user"
        );
        commandService.handle(command);

        // Verificar que el permiso efectivo resuelto es false
        boolean resolved = resolutionService.hasPermission(testUser.getId(), "orders.create");
        assertThat(resolved).isFalse();
    }

    @Test
    public void testDeleteOverrideRestoresDefault() {
        // Registrar anulación individual de revocación
        CreatePermissionOverrideCommand command = new CreatePermissionOverrideCommand(
                testUser.getId(),
                "orders.create",
                OverrideValue.REVOKED,
                "Revocación de prueba",
                "admin_user"
        );
        commandService.handle(command);
        assertThat(resolutionService.hasPermission(testUser.getId(), "orders.create")).isFalse();

        // Eliminar anulación
        DeletePermissionOverrideCommand deleteCmd = new DeletePermissionOverrideCommand(
                testUser.getId(),
                "orders.create",
                "admin_user"
        );
        commandService.handle(deleteCmd);

        // Verificar que se restaura el valor por defecto del rol (true)
        assertThat(resolutionService.hasPermission(testUser.getId(), "orders.create")).isTrue();
    }

    @Test
    public void testCannotOverrideManagePermissions() {
        // Intentar crear override sobre permissions.manage debe lanzar excepción
        CreatePermissionOverrideCommand grantCmd = new CreatePermissionOverrideCommand(
                testUser.getId(),
                "permissions.manage",
                OverrideValue.GRANTED,
                "Intento de hack",
                "admin_user"
        );
        assertThatThrownBy(() -> commandService.handle(grantCmd))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("permissions.manage");

        // Intentar eliminar override sobre permissions.manage debe lanzar excepción
        DeletePermissionOverrideCommand deleteCmd = new DeletePermissionOverrideCommand(
                testUser.getId(),
                "permissions.manage",
                "admin_user"
        );
        assertThatThrownBy(() -> commandService.handle(deleteCmd))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("permissions.manage");
    }

    @Test
    public void testGetMyPermissionsWithOverrides() throws Exception {
        // CASHIER has orders.create = true by default. We apply a REVOKED override.
        CreatePermissionOverrideCommand overrideCmd = new CreatePermissionOverrideCommand(
                testUser.getId(),
                "orders.create",
                OverrideValue.REVOKED,
                "Prueba de anulación en test de integración",
                "admin_user"
        );
        commandService.handle(overrideCmd);

        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        // Perform GET /api/v1/accounts/me/permissions as the CASHIER (testUser)
        mockMvc.perform(get("/api/v1/accounts/me/permissions")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // Verify the override value (granted: false) is returned correctly
                .andExpect(jsonPath("$[?(@.code == 'orders.create')].granted").value(false));
    }

    @Test
    public void testGetMyPermissionsUnauthenticated() throws Exception {
        // Unauthenticated user should not be allowed
        mockMvc.perform(get("/api/v1/accounts/me/permissions"))
                .andExpect(status().isUnauthorized());
    }
}
