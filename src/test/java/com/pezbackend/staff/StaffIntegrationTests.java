package com.pezbackend.staff;

import com.pezbackend.iam.domain.model.entities.Role;
import com.pezbackend.iam.domain.model.aggregates.User;
import com.pezbackend.iam.domain.model.valueobjects.Roles;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pezbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.staff.domain.model.aggregates.StaffProfile;
import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import com.pezbackend.staff.domain.model.entities.PayrollAdjustment;
import com.pezbackend.staff.domain.model.entities.Sanction;
import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import com.pezbackend.staff.domain.model.valueobjects.StaffPaymentType;
import com.pezbackend.staff.domain.model.valueobjects.AttendanceMethod;
import com.pezbackend.staff.domain.model.valueobjects.PayrollAdjustmentType;
import com.pezbackend.staff.domain.model.valueobjects.SanctionType;
import com.pezbackend.staff.infrastructure.persistence.jpa.repositories.*;
import com.pezbackend.shared.domain.model.DomainEvent;
import com.pezbackend.shared.infrastructure.persistence.jpa.repositories.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StaffIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private PayrollAdjustmentRepository payrollAdjustmentRepository;

    @Autowired
    private SanctionRepository sanctionRepository;

    @Autowired
    private OvertimeRecordRepository overtimeRecordRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private EventCollector eventCollector;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        public EventCollector eventCollector() {
            return new EventCollector();
        }
    }

    static class EventCollector {
        private final List<DomainEvent> events = new ArrayList<>();

        @org.springframework.context.event.EventListener
        public void onEvent(DomainEvent event) {
            events.add(event);
        }

        public List<DomainEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }

    private User adminUser;
    private UserDetailsImpl adminDetails;
    private User employeeUser;

    @BeforeEach
    public void setUp() {
        // Limpiar base de datos
        auditEventRepository.deleteAll();
        overtimeRecordRepository.deleteAll();
        sanctionRepository.deleteAll();
        payrollAdjustmentRepository.deleteAll();
        attendanceRecordRepository.deleteAll();
        staffProfileRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Sembrar roles y usuarios de seguridad
        Role adminRole = roleRepository.findByName(Roles.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Roles.ADMIN)));
        Role cookRole = roleRepository.findByName(Roles.COOK)
                .orElseGet(() -> roleRepository.save(new Role(Roles.COOK)));

        adminUser = new User("admin@pez.com", "pw", "Admin", "User", true);
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        adminDetails = UserDetailsImpl.build(adminUser);

        employeeUser = new User("cook@pez.com", "pw", "Cook", "User", true);
        employeeUser.addRole(cookRole);
        employeeUser = userRepository.save(employeeUser);
    }

    @Test
    public void testFingerprintCheckInWithoutConsentFails() throws Exception {
        // 1. Crear perfil para el empleado
        String createProfileJson = String.format(
                "{\"accountId\": %d, \"paymentType\": \"DAILY\", \"agreedAmount\": 150.00}",
                employeeUser.getId()
        );

        String profileResponse = mockMvc.perform(post("/api/v1/staff/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProfileJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extraer id del perfil
        Long profileId = Long.parseLong(com.jayway.jsonpath.JsonPath.read(profileResponse, "$.id").toString());

        // 2. Intentar registrar asistencia por huella (FINGERPRINT_HASH) sin consentimiento
        String checkInJson = String.format(
                "{\"staffProfileId\": %d, \"method\": \"FINGERPRINT_HASH\", \"checkInAt\": \"%s\"}",
                profileId,
                LocalDateTime.now().toString()
        );

        mockMvc.perform(post("/api/v1/staff/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson)
                        .with(user(adminDetails)))
                .andExpect(status().isConflict()) // Violación de regla de negocio
                .andExpect(jsonPath("$.errorCode", is("FINGERPRINT_CONSENT_REQUIRED")));
    }

    @Test
    public void testFingerprintCheckInWithConsentSucceeds() throws Exception {
        // 1. Crear perfil
        StaffProfile profile = new StaffProfile(employeeUser.getId(), StaffPaymentType.DAILY, new BigDecimal("120.00"));
        profile = staffProfileRepository.save(profile);
        Long profileId = profile.getId();

        // 2. Registrar consentimiento
        mockMvc.perform(post("/api/v1/staff/profiles/" + profileId + "/fingerprint-consent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consent\": true}")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fingerprintConsent", is(true)));

        eventCollector.clear();

        // 3. Registrar entrada por huella
        LocalDateTime checkInTime = LocalDateTime.now().withNano(0);
        String checkInJson = String.format(
                "{\"staffProfileId\": %d, \"method\": \"FINGERPRINT_HASH\", \"checkInAt\": \"%s\"}",
                profileId,
                checkInTime.toString()
        );

        mockMvc.perform(post("/api/v1/staff/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method", is("FINGERPRINT_HASH")));

        // Validar que se publicó evento de asistencia
        assertThat(eventCollector.getEvents().stream()
                .anyMatch(e -> e.eventType().equals("AttendanceRecorded"))).isTrue();

        // 4. Registrar salida
        LocalDateTime checkOutTime = checkInTime.plusHours(8);
        String checkOutJson = String.format(
                "{\"staffProfileId\": %d, \"checkOutAt\": \"%s\"}",
                profileId,
                checkOutTime.toString()
        );

        mockMvc.perform(post("/api/v1/staff/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkOutJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // Verificar que el registro de asistencia en BD tiene la salida asignada
        List<AttendanceRecord> records = attendanceRecordRepository.findAllByStaffProfileId(profileId);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getCheckOutAt()).isEqualTo(checkOutTime);
    }

    @Test
    public void testPaymentSummaryCorrectCalculation() throws Exception {
        // 1. Crear perfil con agreedAmount = 1000.00
        StaffProfile profile = new StaffProfile(employeeUser.getId(), StaffPaymentType.MONTHLY, new BigDecimal("1000.00"));
        profile = staffProfileRepository.save(profile);
        Long profileId = profile.getId();

        // 2. Registrar adelanto (ADVANCE) de 200.00
        String advanceJson = "{\"type\": \"ADVANCE\", \"amount\": 200.00, \"date\": \"2026-08-01\"}";
        mockMvc.perform(post("/api/v1/staff/profiles/" + profileId + "/payroll-adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(advanceJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // 3. Registrar consumo (CONSUMPTION_DEDUCTION) de 50.00
        String deductionJson = "{\"type\": \"CONSUMPTION_DEDUCTION\", \"amount\": 50.00, \"date\": \"2026-08-02\"}";
        mockMvc.perform(post("/api/v1/staff/profiles/" + profileId + "/payroll-adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deductionJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // 4. Registrar horas extras de 5.0 horas
        String overtimeJson = "{\"hours\": 5.0, \"date\": \"2026-08-03\"}";
        mockMvc.perform(post("/api/v1/staff/profiles/" + profileId + "/overtime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overtimeJson)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        // 5. Consultar payment summary
        mockMvc.perform(get("/api/v1/staff/profiles/" + profileId + "/payment-summary")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreedAmount", is(1000.00)))
                .andExpect(jsonPath("$.totalAdvances", is(200.00)))
                .andExpect(jsonPath("$.totalDeductions", is(50.00)))
                .andExpect(jsonPath("$.totalOvertimeHours", is(5.00)))
                .andExpect(jsonPath("$.netPending", is(770.8335))); // 1000 - 200 - 50 + (5.0 * 4.1667) = 770.8335
    }
}
