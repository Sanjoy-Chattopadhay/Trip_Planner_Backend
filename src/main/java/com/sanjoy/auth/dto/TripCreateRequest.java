package com.sanjoy.auth.dto;

public class TripCreateRequest {
    private String destination;
    private Double budget;
    private String startDate;
    private String endDate;
    private Boolean femaleAllowed;
    private Integer maleCount;
    private Integer femaleCount;

    // Getters and setters
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Boolean getFemaleAllowed() { return femaleAllowed; }
    public void setFemaleAllowed(Boolean femaleAllowed) { this.femaleAllowed = femaleAllowed; }

    public Integer getMaleCount() { return maleCount; }
    public void setMaleCount(Integer maleCount) { this.maleCount = maleCount; }

    public Integer getFemaleCount() { return femaleCount; }
    public void setFemaleCount(Integer femaleCount) { this.femaleCount = femaleCount; }
}
