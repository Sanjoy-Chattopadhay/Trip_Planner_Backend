package com.sanjoy.auth.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ItineraryService {

    @SystemMessage("You are an expert travel planner. Create detailed day-by-day itineraries " +
            "based on destination, dates, budget, and group composition. " +
            "Format the output with clear day headers, activities, and estimated costs.")
    String generateItinerary(@UserMessage String tripDetails);
}
