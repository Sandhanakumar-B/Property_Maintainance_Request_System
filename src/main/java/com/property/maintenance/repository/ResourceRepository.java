package com.property.maintenance.repository;

import com.property.maintenance.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Resource entity.
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Optional<Resource> findByReference(String reference);
}
