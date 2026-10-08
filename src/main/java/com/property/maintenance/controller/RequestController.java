package com.property.maintenance.controller;

import com.property.maintenance.dto.ApiResponse;
import com.property.maintenance.dto.CreateRequestDto;
import com.property.maintenance.dto.StatusUpdateDto;
import com.property.maintenance.model.RequestStatusHistory;
import com.property.maintenance.model.ServiceRequest;
import com.property.maintenance.service.RequestService;
import com.property.maintenance.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller managing Maintenance Service Request APIs.
 * Satisfies FR1 - FR10 and BR1 - BR10.
 */
@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class RequestController {

    private final RequestService requestService;
    private final ValidationService validationService;

    @Autowired
    public RequestController(RequestService requestService, ValidationService validationService) {
        this.requestService = requestService;
        this.validationService = validationService;
    }

    /**
     * FR3, BR4: Real-time Applicable Charge Calculation Preview
     * Rules:
     * - Rs.50 when chargeable amount is below Rs.1,000.
     * - Rs.0 when chargeable amount is Rs.1,000 or above.
     * - If no chargeable amount applies (0 or null), Rs.0.
     */
    @GetMapping("/calculate-charge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewCharge(
            @RequestParam(required = false) BigDecimal chargeableAmount) {
        BigDecimal charge = validationService.calculateCharge(chargeableAmount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("chargeableAmount", chargeableAmount != null ? chargeableAmount : BigDecimal.ZERO);
        response.put("charge", charge);
        
        return ResponseEntity.ok(ApiResponse.success("Charge calculated", response));
    }

    /**
     * FR1, FR2, FR4, FR5: Submit a new Maintenance Request
     * Validates eligibility, checks active limit, calculates charge, saves snapshot,
     * decrements capacity, and records audit history.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequest>> submitRequest(@RequestBody CreateRequestDto dto) {
        ServiceRequest created = requestService.submitRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance request submitted successfully", created));
    }

    /**
     * FR6: Get all requests belonging to the logged-in user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ServiceRequest>>> getUserRequests(@PathVariable Long userId) {
        List<ServiceRequest> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(ApiResponse.success("User requests retrieved", requests));
    }

    /**
     * FR7: Get all requests (for Administrator)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequest>>> getAllRequests() {
        List<ServiceRequest> requests = requestService.getAllRequests();
        return ResponseEntity.ok(ApiResponse.success("All requests retrieved", requests));
    }

    /**
     * FR6, BR10: Get detailed view of a request (includes snapshot data)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequest>> getRequestDetails(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        ServiceRequest request = requestService.getRequestDetails(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Request details retrieved", request));
    }

    /**
     * Audit Trail: Get Status History for a specific request
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<RequestStatusHistory>>> getRequestHistory(@PathVariable Long id) {
        List<RequestStatusHistory> history = requestService.getRequestHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Request history retrieved", history));
    }

    /**
     * FR7, BR8: Administrator advances status
     * Allowed: SUBMITTED -> APPROVED/ASSIGNED -> IN_PROGRESS -> COMPLETED
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ServiceRequest>> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateDto dto) {
        ServiceRequest updated = requestService.updateStatus(id, dto.getNewStatus(), dto.getActorId());
        return ResponseEntity.ok(ApiResponse.success("Request status updated to " + dto.getNewStatus(), updated));
    }

    /**
     * FR8, BR9, BR10: User cancels request
     * Only SUBMITTED requests can be cancelled; releases resource capacity exactly once.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ServiceRequest>> cancelRequest(
            @PathVariable Long id,
            @RequestParam Long userId) {
        ServiceRequest cancelled = requestService.cancelRequest(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Request cancelled successfully", cancelled));
    }
}
