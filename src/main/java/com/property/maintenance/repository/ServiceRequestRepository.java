package com.property.maintenance.repository;

import com.property.maintenance.model.ServiceRequest;
import com.property.maintenance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for ServiceRequest entity.
 */
@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    
    // Retrieve requests belonging to a specific user
    List<ServiceRequest> findByUserOrderByRequestIdDesc(User user);
    
    // Retrieve requests for a user by user ID
    List<ServiceRequest> findByUser_IdOrderByRequestIdDesc(Long userId);
    
    // Count active requests for a user (BR1 check)
    // Active statuses are SUBMITTED, APPROVED/ASSIGNED, IN_PROGRESS
    long countByUserAndStatusIn(User user, List<String> statuses);

    // Get all requests ordered newest first for Admin view
    List<ServiceRequest> findAllByOrderByRequestIdDesc();
}
