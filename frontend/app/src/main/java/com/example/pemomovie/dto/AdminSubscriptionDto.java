package com.example.pemomovie.dto;

public class AdminSubscriptionDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer durationDays;
    private Boolean isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : false;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
