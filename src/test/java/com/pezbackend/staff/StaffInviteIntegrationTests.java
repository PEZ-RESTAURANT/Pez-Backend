package com.pezbackend.staff;

import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.staff.domain.model.aggregates.StaffInvite;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.StaffInviteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StaffInviteIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StaffInviteRepository staffInviteRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private User adminUser;
    private UserDetailsImpl adminDetails;

    @BeforeEach
    public void setUp() {
        staffInviteRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM user WHERE email = ? OR email = ? OR email = ?)", "admin@pez.com", "mozo@pez.com", "cook@pez.com");
        jdbcTemplate.update("DELETE FROM user WHERE email = ? OR email = ? OR email = ?", "admin@pez.com", "mozo@pez.com", "cook@pez.com");

        // Seed roles only if missing
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));
        roleRepository.findByName(Roles.WAITER)
                .orElseGet(() -> roleRepository.save(new Role(Roles.WAITER)));
        roleRepository.findByName(Roles.COOK)
                .orElseGet(() -> roleRepository.save(new Role(Roles.COOK)));
        roleRepository.findByName(Roles.CASHIER)
                .orElseGet(() -> roleRepository.save(new Role(Roles.CASHIER)));

        adminUser = new User("admin@pez.com", "pw", "Admin", "User", true);
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminDetails = UserDetailsImpl.build(adminUser);
    }

    @Test
    public void testFullInvitationLifecycle() throws Exception {
        // 1. Admin generates an invitation
        String createJson = "{\"requestedRole\": \"WAITER\", \"email\": \"mozo@pez.com\"}";
        String response = mockMvc.perform(post("/api/v1/staff/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .with(user(adminDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", notNullValue()))
                .andExpect(jsonPath("$.requestedRole", is("WAITER")))
                .andExpect(jsonPath("$.email", is("mozo@pez.com")))
                .andExpect(jsonPath("$.used", is(false)))
                .andExpect(jsonPath("$.revoked", is(false)))
                .andExpect(jsonPath("$.restaurantId", is(adminUser.getRestaurantId().intValue())))
                .andReturn().getResponse().getContentAsString();

        String code = com.jayway.jsonpath.JsonPath.read(response, "$.code").toString();

        // 2. Admin gets list of invites
        mockMvc.perform(get("/api/v1/staff/invites")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(code)));

        // 3. Public verifies invitation code (without auth)
        mockMvc.perform(get("/api/v1/staff/invites/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(code)))
                .andExpect(jsonPath("$.requestedRole", is("WAITER")));

        // 4. Public accepts invitation (creates worker account, without auth)
        String acceptJson = "{" +
                "\"firstName\": \"Mozo\"," +
                "\"lastName\": \"Trabajador\"," +
                "\"email\": \"mozo@pez.com\"," +
                "\"password\": \"secure_password_123\"" +
                "}";

        mockMvc.perform(post("/api/v1/staff/invites/" + code + "/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Cuenta de colaborador creada con éxito.")));

        // Verify user was actually created in IAM with correct role and restaurantId
        Optional<User> createdUserOpt = userRepository.findByEmail("mozo@pez.com");
        assertThat(createdUserOpt).isPresent();
        User createdUser = createdUserOpt.get();
        assertThat(createdUser.getFirstName()).isEqualTo("Mozo");
        assertThat(createdUser.getLastName()).isEqualTo("Trabajador");
        assertThat(createdUser.getRestaurantId()).isEqualTo(adminUser.getRestaurantId());
        assertThat(createdUser.getRoles().get(0).getName()).isEqualTo(Roles.WAITER);

        // 5. Try to accept the invitation again (should fail)
        mockMvc.perform(post("/api/v1/staff/invites/" + code + "/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isGone());

        // 6. Test public endpoint with invalid code
        mockMvc.perform(get("/api/v1/staff/invites/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRevokeInvitation() throws Exception {
        // 1. Admin generates invitation
        String createJson = "{\"requestedRole\": \"COOK\", \"email\": \"cook@pez.com\"}";
        String response = mockMvc.perform(post("/api/v1/staff/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .with(user(adminDetails)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String code = com.jayway.jsonpath.JsonPath.read(response, "$.code").toString();

        // 2. Admin revokes invitation
        mockMvc.perform(delete("/api/v1/staff/invites/" + code)
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Invitación revocada con éxito.")));

        // 3. Verify public validation fails for revoked code
        mockMvc.perform(get("/api/v1/staff/invites/" + code))
                .andExpect(status().isGone());

        // 4. Verify public acceptance fails
        String acceptJson = "{" +
                "\"firstName\": \"Cocinero\"," +
                "\"lastName\": \"Prueba\"," +
                "\"email\": \"cook@pez.com\"," +
                "\"password\": \"secure_password_123\"" +
                "}";

        mockMvc.perform(post("/api/v1/staff/invites/" + code + "/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isGone());
    }

    @Test
    public void testInvitationAcceptanceIgnoresPayloadEmail() throws Exception {
        // 1. Admin generates an invitation for a specific email
        String createJson = "{\"requestedRole\": \"WAITER\", \"email\": \"mozo@pez.com\"}";
        String response = mockMvc.perform(post("/api/v1/staff/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .with(user(adminDetails)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String code = com.jayway.jsonpath.JsonPath.read(response, "$.code").toString();

        // 2. Attacker accepts the invitation but changes the email in payload
        String acceptJson = "{" +
                "\"firstName\": \"Mozo\"," +
                "\"lastName\": \"Trabajador\"," +
                "\"email\": \"attacker@pez.com\"," +
                "\"password\": \"secure_password_123\"" +
                "}";

        mockMvc.perform(post("/api/v1/staff/invites/" + code + "/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isCreated());

        // 3. Confirm that the created user has the invited email, NOT the attacker's email
        Optional<User> attackerOpt = userRepository.findByEmail("attacker@pez.com");
        assertThat(attackerOpt).isNotPresent();

        Optional<User> correctUserOpt = userRepository.findByEmail("mozo@pez.com");
        assertThat(correctUserOpt).isPresent();
        assertThat(correctUserOpt.get().getFirstName()).isEqualTo("Mozo");
    }
}
