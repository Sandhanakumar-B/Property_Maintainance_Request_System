package com.property.maintenance.model;

import jakarta.persistence.*;

/**
 * Entity representing a Maintenance Service / Resource (e.g., S101, S102, S103).
 * Satisfies UML Class: Resource
 */
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference; // e.g., 'S101', 'S102', 'S103'

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "available_capacity", nullable = false)
    private Integer availableCapacity;

    @Column(nullable = false, length = 20)
    private String status; // 'ACTIVE' or 'INACTIVE'

    public Resource() {
    }

    public Resource(Long id, String reference, String name, Integer capacity, Integer availableCapacity, String status) {
        this.id = id;
        this.reference = reference;
        this.name = name;
        this.capacity = capacity;
        this.availableCapacity = availableCapacity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }
}
