package com.property.maintenance.service;

import com.property.maintenance.dto.CreateRequestDto;
import com.property.maintenance.model.Resource;
import com.property.maintenance.model.User;
import com.property.maintenance.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for business rule validation.
 * Satisfies UML Class: ValidationService
 */
@Service
public class ValidationService {

    private final ServiceRequestRepository requestRepository;

    // Active statuses defined by BR1 & BR8
    public static final List<String> ACTIVE_STATUSES = Arrays.asList(
            "SUBMITTED", 
            "APPROVED/ASSIGNED", 
            "IN_PROGRESS"
    );

    @Autowired
    public ValidationService(ServiceRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    /**
     * BR2: Validate User Eligibility
     */
    public void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("User is inactive.");
        }
    }

    /**
     * BR2: Validate Resource/Service Eligibility and Capacity
     */
    public void validateService(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found.");
        }
        if (!resource.isActive()) {
            throw new IllegalArgumentException("Resource is inactive.");
        }
        if (resource.getAvailableCapacity() == null || resource.getAvailableCapacity() <= 0) {
            throw new IllegalArgumentException("No available capacity for this resource.");
        }
    }

    /**
     * BR1: Active Request Limit (Maximum 5 active requests allowed; 6th rejected)
     */
    public void checkActiveRequestLimit(User user) {
        if (user == null) {
            return;
        }
        long activeCount = requestRepository.countByUserAndStatusIn(user, ACTIVE_STATUSES);
        if (activeCount >= 5) {
            throw new IllegalArgumentException("Maximum 5 active requests allowed.");
        }
    }

    /**
     * BR5: Phone validation - must contain exactly 10 digits
     */
    public void validatePhone(String phone) {
        if (phone == null || !phone.trim().matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Invalid phone number. Must contain exactly 10 digits.");
        }
    }

    /**
     * BR5: Required details validation
     */
    public void validateRequiredDetails(String name, String phone, String description, String reference) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        validatePhone(phone);
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be blank.");
        }
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Reference cannot be blank.");
        }
    }

    /**
     * Comprehensive validation before submission
     */
    public void validateRequest(CreateRequestDto dto, User user, Resource resource) {
        if (dto == null) {
            throw new IllegalArgumentException("Request data cannot be empty.");
        }
        // BR5 Details Validation
        validateRequiredDetails(dto.getName(), dto.getPhone(), dto.getDescription(), dto.getReference());
        // BR2 User Validation
        validateUser(user);
        // BR2 Service Validation
        validateService(resource);
        // BR1 Active Limit Check
        checkActiveRequestLimit(user);
    }

    /**
     * BR4: Calculate Applicable Charge
     * - Rs.50 when chargeable amount is below Rs.1,000.
     * - Rs.0 when chargeable amount is Rs.1,000 or above.
     * - If no chargeable amount applies, store Rs.0.
     */
    public BigDecimal calculateCharge(BigDecimal chargeableAmount) {
        if (chargeableAmount == null || chargeableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (chargeableAmount.compareTo(new BigDecimal("1000.00")) < 0) {
            return new BigDecimal("50.00");
        } else {
            return BigDecimal.ZERO.setScale(2);
        }
    }
}
