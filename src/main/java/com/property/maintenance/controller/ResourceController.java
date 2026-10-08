package com.property.maintenance.controller;

import com.property.maintenance.dto.ApiResponse;
import com.property.maintenance.model.Resource;
import com.property.maintenance.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Maintenance Services / Resources (S101, S102, S103).
 */
@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceRepository resourceRepository;

    @Autowired
    public ResourceController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    /**
     * Get all resources / maintenance services with their available capacity and status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Resource>>> getAllResources() {
        List<Resource> resources = resourceRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", resources));
    }

    /**
     * Get resource by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Resource>> getResourceById(@PathVariable Long id) {
        return resourceRepository.findById(id)
                .map(resource -> ResponseEntity.ok(ApiResponse.success("Resource found", resource)))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with id: " + id));
    }
}
