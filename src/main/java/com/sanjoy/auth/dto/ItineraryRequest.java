package com.sanjoy.auth.dto;

public class ItineraryRequest {
    private String destination;
    private String startDate;  // ISO yyyy-MM-dd
    private String endDate;
    private Double budget;
    private Integer maleCount;
    private Integer femaleCount;
    private String notes;      // text from the textbox

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Integer getMaleCount() {
        return maleCount;
    }

    public void setMaleCount(Integer maleCount) {
        this.maleCount = maleCount;
    }

    public Integer getFemaleCount() {
        return femaleCount;
    }

    public void setFemaleCount(Integer femaleCount) {
        this.femaleCount = femaleCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // getters and setters
}
