package com.property.maintenance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.property.maintenance.dto.CreateRequestDto;
import com.property.maintenance.model.RequestStatusHistory;
import com.property.maintenance.model.Resource;
import com.property.maintenance.model.ServiceRequest;
import com.property.maintenance.model.User;
import com.property.maintenance.repository.RequestStatusHistoryRepository;
import com.property.maintenance.repository.ResourceRepository;
import com.property.maintenance.repository.ServiceRequestRepository;
import com.property.maintenance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing Maintenance Requests, Status Transitions, and Transactions.
 * Satisfies UML Class: RequestService
 * Demonstrates dependency: ValidationService ---> RequestService
 */
@Service
public class RequestService {

    private final ValidationService validationService;
    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public RequestService(ValidationService validationService,
                          ServiceRequestRepository requestRepository,
                          UserRepository userRepository,
                          ResourceRepository resourceRepository,
                          RequestStatusHistoryRepository historyRepository) {
        this.validationService = validationService;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.historyRepository = historyRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * FR1, FR2, FR4, FR5, BR3, BR6, BR7: Atomic Submission of a Maintenance Request
     */
    @Transactional
    public ServiceRequest submitRequest(CreateRequestDto dto) {
        // 1. Fetch User and Resource
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        // 2. Perform exhaustive validation (BR1, BR2, BR5)
        // Eligibility checked immediately before acceptance/submission (BR2)
        validationService.validateRequest(dto, user, resource);

        // 3. Calculate Applicable Charge (BR4)
        BigDecimal charge = validationService.calculateCharge(dto.getChargeableAmount());

        // 4. Generate Snapshot Data (BR3)
        String snapshotData = buildSnapshotData(dto, user, resource, charge);

        // 5. Decrement resource capacity (BR7)
        resource.setAvailableCapacity(resource.getAvailableCapacity() - 1);
        resourceRepository.save(resource);

        // 6. Create and persist ServiceRequest
        ServiceRequest request = new ServiceRequest();
        request.setUser(user);
        request.setResource(resource);
        request.setServiceType(dto.getServiceType() != null ? dto.getServiceType() : resource.getName());
        request.setDescription(dto.getDescription().trim());
        request.setReference(dto.getReference().trim());
        request.setCharge(charge);
        request.setStatus("SUBMITTED"); // Initial status (BR8)
        request.setSubmittedDate(LocalDateTime.now());
        request.setSnapshotData(snapshotData);
        request.setCreatedAt(LocalDateTime.now());

        ServiceRequest savedRequest = requestRepository.save(request);

        // 7. Record initial Status History (BR7, FR5)
        RequestStatusHistory history = new RequestStatusHistory();
        history.setRequest(savedRequest);
        history.setActor(user);
        history.setOldStatus(null);
        history.setNewStatus("SUBMITTED");
        history.setEventAt(LocalDateTime.now());
        historyRepository.save(history);

        return savedRequest;
    }

    /**
     * FR6, BR10: Get requests for a specific user
     */
    public List<ServiceRequest> getUserRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return requestRepository.findByUserOrderByRequestIdDesc(user);
    }

    /**
     * Get all requests (for Administrator)
     */
    public List<ServiceRequest> getAllRequests() {
        return requestRepository.findAllByOrderByRequestIdDesc();
    }

    /**
     * FR6, BR10: Get Request Details with security validation
     */
    public ServiceRequest getRequestDetails(Long requestId, Long requesterUserId, boolean isAdmin) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        // BR10 Security: Users can access only their own requests
        if (!isAdmin && !request.getUser().getId().equals(requesterUserId)) {
            throw new SecurityException("Unauthorized access.");
        }

        return request;
    }

    /**
     * Get audit history for a request
     */
    public List<RequestStatusHistory> getRequestHistory(Long requestId) {
        return historyRepository.findByRequest_RequestIdOrderByEventAtAsc(requestId);
    }

    /**
     * FR7, BR8, BR10: Administrator Status Transition (Atomic)
     * Allowed: SUBMITTED -> APPROVED/ASSIGNED -> IN_PROGRESS -> COMPLETED
     */
    @Transactional
    public ServiceRequest updateStatus(Long requestId, String targetStatus, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found."));

        if (!"ADMINISTRATOR".equalsIgnoreCase(actor.getRole())) {
            throw new SecurityException("Unauthorized access. Only Administrators can advance status.");
        }

        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        String currentStatus = request.getStatus();

        // Validate exact transition sequence (BR8 - Skipping NOT allowed)
        boolean isValidTransition = false;
        if ("SUBMITTED".equalsIgnoreCase(currentStatus) && "APPROVED/ASSIGNED".equalsIgnoreCase(targetStatus)) {
            isValidTransition = true;
        } else if ("APPROVED/ASSIGNED".equalsIgnoreCase(currentStatus) && "IN_PROGRESS".equalsIgnoreCase(targetStatus)) {
            isValidTransition = true;
        } else if ("IN_PROGRESS".equalsIgnoreCase(currentStatus) && "COMPLETED".equalsIgnoreCase(targetStatus)) {
            isValidTransition = true;
        }

        if (!isValidTransition) {
            throw new IllegalArgumentException("Invalid status transition: Cannot move from " 
                    + currentStatus + " to " + targetStatus + ".");
        }

        request.setStatus(targetStatus);
        ServiceRequest updatedRequest = requestRepository.save(request);

        // Record history
        RequestStatusHistory history = new RequestStatusHistory();
        history.setRequest(updatedRequest);
        history.setActor(actor);
        history.setOldStatus(currentStatus);
        history.setNewStatus(targetStatus);
        history.setEventAt(LocalDateTime.now());
        historyRepository.save(history);

        return updatedRequest;
    }

    /**
     * FR8, BR9, BR10: Cancel Request and Release Resource (Atomic)
     * Only SUBMITTED requests can be cancelled. Releases resource exactly once.
     */
    @Transactional
    public ServiceRequest cancelRequest(Long requestId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        // BR10 Security check
        if (!request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access. You can only cancel your own requests.");
        }

        // BR8 & BR9 check: User may cancel ONLY a SUBMITTED request
        if ("CANCELLED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Request is already cancelled.");
        }
        if (!"SUBMITTED".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Request cannot be cancelled. Only SUBMITTED requests can be cancelled.");
        }

        String oldStatus = request.getStatus();
        request.setStatus("CANCELLED");
        ServiceRequest savedRequest = requestRepository.save(request);

        // BR9: Release reserved resource capacity exactly once
        Resource resource = request.getResource();
        if (resource != null) {
            resource.setAvailableCapacity(resource.getAvailableCapacity() + 1);
            resourceRepository.save(resource);
        }

        // Record cancellation in history
        RequestStatusHistory history = new RequestStatusHistory();
        history.setRequest(savedRequest);
        history.setActor(user);
        history.setOldStatus(oldStatus);
        history.setNewStatus("CANCELLED");
        history.setEventAt(LocalDateTime.now());
        historyRepository.save(history);

        return savedRequest;
    }

    /**
     * Helper to build JSON Snapshot of submitted details (BR3)
     */
    private String buildSnapshotData(CreateRequestDto dto, User user, Resource resource, BigDecimal charge) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("userId", user.getId());
            root.put("userName", user.getName());
            root.put("userEmail", user.getEmail());
            root.put("userPhone", user.getPhone());
            root.put("resourceId", resource.getId());
            root.put("resourceReference", resource.getReference());
            root.put("resourceName", resource.getName());
            root.put("serviceType", dto.getServiceType() != null ? dto.getServiceType() : resource.getName());
            root.put("description", dto.getDescription());
            root.put("reference", dto.getReference());
            root.put("chargeableAmount", dto.getChargeableAmount() != null ? dto.getChargeableAmount().toString() : "0");
            root.put("charge", charge.toString());
            root.put("snapshotTimestamp", LocalDateTime.now().toString());
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"name\":\"" + dto.getName() + "\",\"reference\":\"" + dto.getReference() + "\"}";
        }
    }
}
