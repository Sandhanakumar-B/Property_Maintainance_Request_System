package com.property.maintenance;

import com.property.maintenance.dto.CreateRequestDto;
import com.property.maintenance.model.RequestStatusHistory;
import com.property.maintenance.model.Resource;
import com.property.maintenance.model.ServiceRequest;
import com.property.maintenance.model.User;
import com.property.maintenance.repository.RequestStatusHistoryRepository;
import com.property.maintenance.repository.ResourceRepository;
import com.property.maintenance.repository.ServiceRequestRepository;
import com.property.maintenance.repository.UserRepository;
import com.property.maintenance.service.RequestService;
import com.property.maintenance.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mandatory Assessment Test Suite (T1 - T12).
 * Validates all Business Rules (BR1 - BR10) and Functional Requirements (FR1 - FR10).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PropertyMaintenanceApplicationTests {

    @Autowired
    private RequestService requestService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private RequestStatusHistoryRepository historyRepository;

    private User testUser;
    private User inactiveUser;
    private User adminUser;
    private Resource activeResource;
    private Resource zeroCapResource;
    private Resource inactiveResource;

    @BeforeEach
    void setUp() {
        // Clear previous state
        historyRepository.deleteAllInBatch();
        requestRepository.deleteAllInBatch();
        resourceRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Create test actors
        testUser = userRepository.saveAndFlush(new User(null, "USER1", "0771234567", "user1@test.lk", "USER", "ACTIVE"));
        inactiveUser = userRepository.saveAndFlush(new User(null, "INACTIVE_USER", "0779999999", "inactive@test.lk", "USER", "INACTIVE"));
        adminUser = userRepository.saveAndFlush(new User(null, "ADMIN1", "0770000000", "admin@test.lk", "ADMINISTRATOR", "ACTIVE"));

        // Create test resources
        activeResource = resourceRepository.saveAndFlush(new Resource(null, "S101", "Electrical Maintenance", 10, 10, "ACTIVE"));
        zeroCapResource = resourceRepository.saveAndFlush(new Resource(null, "S102", "Plumbing Full", 5, 0, "ACTIVE"));
        inactiveResource = resourceRepository.saveAndFlush(new Resource(null, "S103", "HVAC Inactive", 5, 5, "INACTIVE"));
    }

    /**
     * T1: Validate Active Request Limit (BR1)
     * Maximum 5 active requests allowed; 6th submission must be rejected.
     */
    @Test
    @DisplayName("T1: Max 5 active requests allowed (6th rejected)")
    void testT1_ActiveRequestLimit() {
        // Create 5 active requests
        for (int i = 1; i <= 5; i++) {
            CreateRequestDto dto = new CreateRequestDto(
                    testUser.getId(), activeResource.getId(), testUser.getName(),
                    testUser.getPhone(), "APT-" + i, "Service", "Issue " + i, BigDecimal.ZERO
            );
            assertNotNull(requestService.submitRequest(dto));
        }

        // 6th submission must throw IllegalArgumentException
        CreateRequestDto dto6 = new CreateRequestDto(
                testUser.getId(), activeResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-6", "Service", "Issue 6", BigDecimal.ZERO
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            requestService.submitRequest(dto6);
        });

        assertTrue(ex.getMessage().contains("Maximum 5 active requests allowed"));
    }

    /**
     * T2: Reject Request for Inactive User (BR2)
     */
    @Test
    @DisplayName("T2: Inactive user request rejection")
    void testT2_InactiveUserRejection() {
        CreateRequestDto dto = new CreateRequestDto(
                inactiveUser.getId(), activeResource.getId(), inactiveUser.getName(),
                inactiveUser.getPhone(), "APT-1", "Service", "Issue", BigDecimal.ZERO
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            requestService.submitRequest(dto);
        });

        assertTrue(ex.getMessage().contains("User is inactive"));
    }

    /**
     * T3: Reject Request for Inactive Resource/Service (BR2)
     */
    @Test
    @DisplayName("T3: Inactive resource request rejection")
    void testT3_InactiveResourceRejection() {
        CreateRequestDto dto = new CreateRequestDto(
                testUser.getId(), inactiveResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-1", "Service", "Issue", BigDecimal.ZERO
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            requestService.submitRequest(dto);
        });

        assertTrue(ex.getMessage().contains("Resource is inactive"));
    }

    /**
     * T4: Reject Request when Resource has Zero Available Capacity (BR2)
     */
    @Test
    @DisplayName("T4: Zero capacity resource rejection")
    void testT4_ZeroCapacityResourceRejection() {
        CreateRequestDto dto = new CreateRequestDto(
                testUser.getId(), zeroCapResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-1", "Service", "Issue", BigDecimal.ZERO
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            requestService.submitRequest(dto);
        });

        assertTrue(ex.getMessage().contains("No available capacity"));
    }

    /**
     * T5: Applicable Charge for Amount below Rs. 1,000 is Rs. 50 (BR4)
     */
    @Test
    @DisplayName("T5: Charge calculation for amount < 1000 (Expect Rs. 50.00)")
    void testT5_ChargeCalculationBelow1000() {
        BigDecimal charge = validationService.calculateCharge(new BigDecimal("999.99"));
        assertEquals(new BigDecimal("50.00"), charge);

        BigDecimal chargeSmall = validationService.calculateCharge(new BigDecimal("250.00"));
        assertEquals(new BigDecimal("50.00"), chargeSmall);
    }

    /**
     * T6: Applicable Charge for Amount >= Rs. 1,000 is Rs. 0 (BR4)
     */
    @Test
    @DisplayName("T6: Charge calculation for amount >= 1000 (Expect Rs. 0.00)")
    void testT6_ChargeCalculation1000OrAbove() {
        BigDecimal charge1000 = validationService.calculateCharge(new BigDecimal("1000.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(charge1000));

        BigDecimal charge5000 = validationService.calculateCharge(new BigDecimal("5000.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(charge5000));
    }

    /**
     * T7: Applicable Charge for Zero or Null Amount is Rs. 0 (BR4)
     */
    @Test
    @DisplayName("T7: Charge calculation for zero or null amount (Expect Rs. 0.00)")
    void testT7_ChargeCalculationZeroOrNull() {
        BigDecimal chargeZero = validationService.calculateCharge(BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(chargeZero));

        BigDecimal chargeNull = validationService.calculateCharge(null);
        assertEquals(0, BigDecimal.ZERO.compareTo(chargeNull));
    }

    /**
     * T8: Phone Number Validation (BR5)
     * Must contain exactly 10 digits.
     */
    @Test
    @DisplayName("T8: Phone number validation (10 digits required)")
    void testT8_PhoneValidation() {
        // Valid 10 digits
        assertDoesNotThrow(() -> validationService.validatePhone("0771234567"));

        // Invalid: 9 digits
        IllegalArgumentException ex9 = assertThrows(IllegalArgumentException.class, () -> {
            validationService.validatePhone("077123456");
        });
        assertTrue(ex9.getMessage().contains("10 digits"));

        // Invalid: Characters
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.validatePhone("077123456A");
        });
    }

    /**
     * T9: Successful Request Creation, Initial SUBMITTED Status & Capacity Decrement (BR6, BR7)
     */
    @Test
    @DisplayName("T9: Successful submission sets SUBMITTED and decrements capacity")
    void testT9_SuccessfulRequestCreationAndCapacityDecrement() {
        int initialCapacity = activeResource.getAvailableCapacity();

        CreateRequestDto dto = new CreateRequestDto(
                testUser.getId(), activeResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-101", "Electrical", "Light bulb replacement", new BigDecimal("500.00")
        );

        ServiceRequest request = requestService.submitRequest(dto);

        assertNotNull(request.getRequestId());
        assertEquals("SUBMITTED", request.getStatus());
        assertEquals(new BigDecimal("50.00"), request.getCharge());
        assertNotNull(request.getSnapshotData());
        assertTrue(request.getSnapshotData().contains("USER1"));

        // Verify capacity decremented
        Resource updatedResource = resourceRepository.findById(activeResource.getId()).orElseThrow();
        assertEquals(initialCapacity - 1, updatedResource.getAvailableCapacity());
    }

    /**
     * T10: Audit Trail Recording on Submission and Transitions (FR5, BR7)
     */
    @Test
    @DisplayName("T10: Audit trail history recording")
    void testT10_AuditTrailRecording() {
        CreateRequestDto dto = new CreateRequestDto(
                testUser.getId(), activeResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-202", "Electrical", "Wiring check", BigDecimal.ZERO
        );

        ServiceRequest request = requestService.submitRequest(dto);

        List<RequestStatusHistory> history = historyRepository.findByRequest_RequestIdOrderByEventAtAsc(request.getRequestId());
        assertEquals(1, history.size());
        assertNull(history.get(0).getOldStatus());
        assertEquals("SUBMITTED", history.get(0).getNewStatus());
        assertEquals(testUser.getId(), history.get(0).getActor().getId());
    }

    /**
     * T11: Strict Sequential Status Transitions Enforcement (BR8)
     * Sequence: SUBMITTED -> APPROVED/ASSIGNED -> IN_PROGRESS -> COMPLETED
     * Skipping steps must be rejected.
     */
    @Test
    @DisplayName("T11: Sequential status transition enforcement & skip rejection")
    void testT11_StatusTransitionEnforcement() {
        CreateRequestDto dto = new CreateRequestDto(
                testUser.getId(), activeResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-303", "Electrical", "Switch fix", BigDecimal.ZERO
        );

        ServiceRequest req = requestService.submitRequest(dto);

        // Illegal: SUBMITTED -> IN_PROGRESS (Skipping APPROVED/ASSIGNED)
        assertThrows(IllegalArgumentException.class, () -> {
            requestService.updateStatus(req.getRequestId(), "IN_PROGRESS", adminUser.getId());
        });

        // Legal: SUBMITTED -> APPROVED/ASSIGNED
        ServiceRequest step1 = requestService.updateStatus(req.getRequestId(), "APPROVED/ASSIGNED", adminUser.getId());
        assertEquals("APPROVED/ASSIGNED", step1.getStatus());

        // Legal: APPROVED/ASSIGNED -> IN_PROGRESS
        ServiceRequest step2 = requestService.updateStatus(req.getRequestId(), "IN_PROGRESS", adminUser.getId());
        assertEquals("IN_PROGRESS", step2.getStatus());

        // Legal: IN_PROGRESS -> COMPLETED
        ServiceRequest step3 = requestService.updateStatus(req.getRequestId(), "COMPLETED", adminUser.getId());
        assertEquals("COMPLETED", step3.getStatus());
    }

    /**
     * T12: Request Cancellation & Resource Capacity Release (BR8, BR9, BR10)
     * Only SUBMITTED requests can be cancelled; releases capacity exactly once.
     */
    @Test
    @DisplayName("T12: Cancellation releases capacity; non-SUBMITTED cancel rejected")
    void testT12_CancellationAndCapacityRelease() {
        CreateRequestDto dto = new CreateRequestDto(
                testUser.getId(), activeResource.getId(), testUser.getName(),
                testUser.getPhone(), "APT-404", "Electrical", "Doorbell issue", BigDecimal.ZERO
        );

        ServiceRequest req = requestService.submitRequest(dto);
        int capAfterSubmit = resourceRepository.findById(activeResource.getId()).orElseThrow().getAvailableCapacity();

        // 1. Cancel SUBMITTED request -> Success & capacity released
        ServiceRequest cancelled = requestService.cancelRequest(req.getRequestId(), testUser.getId());
        assertEquals("CANCELLED", cancelled.getStatus());

        int capAfterCancel = resourceRepository.findById(activeResource.getId()).orElseThrow().getAvailableCapacity();
        assertEquals(capAfterSubmit + 1, capAfterCancel);

        // 2. Repeated cancellation rejected
        assertThrows(IllegalArgumentException.class, () -> {
            requestService.cancelRequest(req.getRequestId(), testUser.getId());
        });

        // 3. Cancel non-SUBMITTED request (e.g. IN_PROGRESS) rejected
        ServiceRequest req2 = requestService.submitRequest(dto);
        requestService.updateStatus(req2.getRequestId(), "APPROVED/ASSIGNED", adminUser.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            requestService.cancelRequest(req2.getRequestId(), testUser.getId());
        });
    }
}
