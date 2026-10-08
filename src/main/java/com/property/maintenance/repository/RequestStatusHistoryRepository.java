package com.property.maintenance.repository;

import com.property.maintenance.model.RequestStatusHistory;
import com.property.maintenance.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for RequestStatusHistory entity.
 */
@Repository
public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {
    
    // Retrieve complete status change trail in chronological order
    List<RequestStatusHistory> findByRequestOrderByEventAtAsc(ServiceRequest request);
    
    List<RequestStatusHistory> findByRequest_RequestIdOrderByEventAtAsc(Long requestId);
}
