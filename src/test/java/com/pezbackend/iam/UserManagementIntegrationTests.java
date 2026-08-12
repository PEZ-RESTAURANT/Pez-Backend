package com.pezbackend.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.interfaces.rest.resources.SignUpResource;
import com.pezbackend.iam.interfaces.rest.resources.UpdateUserResource;
import com.pezbackend.tenancy.domain.model.aggregates.Restaurant;
import com.pezbackend.tenancy.infrastructure.persistence.jpa.repositories.RestaurantRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserManagementIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Restaurant restaurantA;
    private Restaurant restaurantB;
    private User adminA;
    private User adminB;
    private Role adminRole;
    private Role waiterRole;

    @BeforeEach
    public void setUp() {
        // Ensure roles exist
        adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));
        waiterRole = roleRepository.findByName(Roles.WAITER)
                .orElseGet(() -> roleRepository.save(new Role(Roles.WAITER)));

        // Create Restaurants (Tenants)
        restaurantA = restaurantRepository.save(new Restaurant("Restaurant A", "12345678901", "a@test.com", "999999999"));
        restaurantB = restaurantRepository.save(new Restaurant("Restaurant B", "12345678902", "b@test.com", "999999998"));

        // Create Admin for Restaurant A
        adminA = new User("admin_a@test.com", "hashed_password", "Admin", "A", true);
        adminA.setRestaurantId(restaurantA.getId());
        adminA.addRole(adminRole);
        adminA = userRepository.save(adminA);

        // Create Admin for Restaurant B
        adminB = new User("admin_b@test.com", "hashed_password", "Admin", "B", true);
        adminB.setRestaurantId(restaurantB.getId());
        adminB.addRole(adminRole);
        adminB = userRepository.save(adminB);
    }

    @Test
    public void testUserCreationAndTenantIsolation() throws Exception {
        // 1. Create a staff user under Restaurant A
        SignUpResource signUpResource = new SignUpResource(
                "worker_a@test.com",
                "password123",
                "Pedro",
                "Picapiedra",
                Roles.WAITER
        );

        UserDetailsImpl adminDetailsA = UserDetailsImpl.build(adminA);

        mockMvc.perform(post("/api/v1/users")
                        .with(user(adminDetailsA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("worker_a@test.com"))
                .andExpect(jsonPath("$.firstName").value("Pedro"))
                .andExpect(jsonPath("$.lastName").value("Picapiedra"))
                .andExpect(jsonPath("$.active").value(true));

        // Verify the created user has restaurantA's ID
        User createdUser = userRepository.findByEmail("worker_a@test.com")
                .orElseThrow(() -> new AssertionError("User should have been created"));
        assertThat(createdUser.getRestaurantId()).isEqualTo(restaurantA.getId());

        // 2. Query all users as Admin A
        mockMvc.perform(get("/api/v1/users")
                        .with(user(adminDetailsA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // adminA + worker_a
                .andExpect(jsonPath("$[?(@.email == 'worker_a@test.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'admin_b@test.com')]").doesNotExist()); // No cross-tenant leak

        // 3. Query all users as Admin B
        UserDetailsImpl adminDetailsB = UserDetailsImpl.build(adminB);
        mockMvc.perform(get("/api/v1/users")
                        .with(user(adminDetailsB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // only adminB
                .andExpect(jsonPath("$[?(@.email == 'worker_a@test.com')]").doesNotExist()); // Confirms isolation
    }

    @Test
    public void testUserUpdateAndDeactivation() throws Exception {
        // Create worker under Restaurant A
        User worker = new User("worker_a@test.com", "hashed_password", "Pedro", "Picapiedra", true);
        worker.setRestaurantId(restaurantA.getId());
        worker.addRole(waiterRole);
        worker = userRepository.save(worker);

        UserDetailsImpl adminDetailsA = UserDetailsImpl.build(adminA);

        // Update name, role, and deactivate
        UpdateUserResource updateResource = new UpdateUserResource(
                "worker_a_updated@test.com",
                "Pedro Modificado",
                "Picapiedra Modificado",
                Roles.ADMIN,
                false // deactivate
        );

        mockMvc.perform(put("/api/v1/users/" + worker.getId())
                        .with(user(adminDetailsA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("worker_a_updated@test.com"))
                .andExpect(jsonPath("$.firstName").value("Pedro Modificado"))
                .andExpect(jsonPath("$.lastName").value("Picapiedra Modificado"))
                .andExpect(jsonPath("$.active").value(false));

        // Verify changes in DB
        User updatedUser = userRepository.findById(worker.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("worker_a_updated@test.com");
        assertThat(updatedUser.getFirstName()).isEqualTo("Pedro Modificado");
        assertThat(updatedUser.getActive()).isFalse();
        assertThat(updatedUser.getRoles().get(0).getName()).isEqualTo(Roles.ADMIN);
    }

    @Test
    public void testGetUserByIdSuccess() throws Exception {
        // Create worker under Restaurant A
        User worker = new User("worker_a@test.com", "hashed_password", "Pedro", "Picapiedra", true);
        worker.setRestaurantId(restaurantA.getId());
        worker.addRole(waiterRole);
        worker = userRepository.save(worker);

        UserDetailsImpl adminDetailsA = UserDetailsImpl.build(adminA);

        mockMvc.perform(get("/api/v1/users/" + worker.getId())
                        .with(user(adminDetailsA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("worker_a@test.com"))
                .andExpect(jsonPath("$.firstName").value("Pedro"))
                .andExpect(jsonPath("$.lastName").value("Picapiedra"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    public void testGetUserByIdNotFound() throws Exception {
        UserDetailsImpl adminDetailsA = UserDetailsImpl.build(adminA);

        mockMvc.perform(get("/api/v1/users/999999")
                        .with(user(adminDetailsA)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserByIdTenantMismatch() throws Exception {
        // Create worker under Restaurant B (different tenant)
        User workerB = new User("worker_b@test.com", "hashed_password", "Pablo", "Marmol", true);
        workerB.setRestaurantId(restaurantB.getId());
        workerB.addRole(waiterRole);
        workerB = userRepository.save(workerB);

        // Authenticate as Admin A (different tenant than workerB)
        UserDetailsImpl adminDetailsA = UserDetailsImpl.build(adminA);

        mockMvc.perform(get("/api/v1/users/" + workerB.getId())
                        .with(user(adminDetailsA)))
                .andExpect(status().isNotFound()); // returns 404 to avoid leaking existence
    }
}
