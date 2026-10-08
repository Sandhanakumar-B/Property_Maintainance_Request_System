package com.property.maintenance.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a new Maintenance Service Request.
 */
public class CreateRequestDto {
    private Long userId;
    private Long resourceId;
    private String name;
    private String phone;
    private String serviceType;
    private String description;
    private String reference;
    private BigDecimal chargeableAmount; // Used to compute applicable charge (BR4)

    public CreateRequestDto() {
    }

    public CreateRequestDto(Long userId, Long resourceId, String name, String phone, 
                            String reference, String serviceType, String description, BigDecimal chargeableAmount) {
        this.userId = userId;
        this.resourceId = resourceId;
        this.name = name;
        this.phone = phone;
        this.reference = reference;
        this.serviceType = serviceType;
        this.description = description;
        this.chargeableAmount = chargeableAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public BigDecimal getChargeableAmount() {
        return chargeableAmount;
    }

    public void setChargeableAmount(BigDecimal chargeableAmount) {
        this.chargeableAmount = chargeableAmount;
    }
}
