package com.property.maintenance.controller;

import com.property.maintenance.dto.ApiResponse;
import com.property.maintenance.model.User;
import com.property.maintenance.repository.ServiceRequestRepository;
import com.property.maintenance.repository.UserRepository;
import com.property.maintenance.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for User information and simulated login/role-selection.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final ServiceRequestRepository requestRepository;

    @Autowired
    public UserController(UserRepository userRepository, ServiceRequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    /**
     * Get all users for simulated role switching (User 1, User 2, Admin 1)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User found", user)))
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    /**
     * Get active request count and eligibility status for a user (BR1, BR2)
     */
    @GetMapping("/{id}/eligibility")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserEligibility(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        long activeCount = requestRepository.countByUserAndStatusIn(user, ValidationService.ACTIVE_STATUSES);
        boolean canCreateMore = user.isActive() && activeCount < 5;

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("userName", user.getName());
        result.put("role", user.getRole());
        result.put("userStatus", user.getStatus());
        result.put("activeRequestCount", activeCount);
        result.put("maxActiveAllowed", 5);
        result.put("canSubmitRequest", canCreateMore);

        return ResponseEntity.ok(ApiResponse.success("Eligibility checked", result));
    }
}
