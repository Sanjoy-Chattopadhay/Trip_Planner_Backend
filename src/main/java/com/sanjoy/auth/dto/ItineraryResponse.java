package com.sanjoy.auth.dto;

public class ItineraryResponse {
    private String itinerary;

    public ItineraryResponse() {}
    public ItineraryResponse(String itinerary) {
        this.itinerary = itinerary;
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }
}
