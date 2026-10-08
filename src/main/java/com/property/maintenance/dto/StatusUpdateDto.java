package com.property.maintenance.dto;

/**
 * Data Transfer Object for Admin status update requests.
 */
public class StatusUpdateDto {
    private String newStatus;
    private Long actorId;

    public StatusUpdateDto() {
    }

    public StatusUpdateDto(String newStatus, Long actorId) {
        this.newStatus = newStatus;
        this.actorId = actorId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }
}
