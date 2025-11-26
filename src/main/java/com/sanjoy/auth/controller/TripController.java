package com.sanjoy.auth.controller;

import com.sanjoy.auth.dto.ItineraryRequest;
import com.sanjoy.auth.dto.ItineraryResponse;
import com.sanjoy.auth.service.ItineraryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.sanjoy.auth.model.Trip;
import com.sanjoy.auth.model.User;
import com.sanjoy.auth.repository.TripRepository;
import com.sanjoy.auth.repository.UserRepository;
import com.sanjoy.auth.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItineraryService itineraryService;


    @PostMapping("/create")
    public Trip createTrip(@RequestBody Trip trip, @AuthenticationPrincipal OAuth2User principal) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        trip.setCreator(user);
        trip.setStatus("Pending");
        return tripRepository.save(trip);
    }

    @GetMapping("/my")
    public List<Trip> myTrips(@AuthenticationPrincipal OAuth2User principal) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return tripRepository.findByCreatorId(user.getId());
    }

    @GetMapping("/all")
    public List<Trip> allTrips() {
        return tripRepository.findAll();
    }

    @PostMapping("/{id}/join")
    public Trip joinTrip(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        String email = getEmailFromPrincipal(principal);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        trip.getMembers().add(user);
        return tripRepository.save(trip);
    }

    @PostMapping("/{id}/status")
    public Trip approveOrDeny(@PathVariable Long id, @RequestParam String action,
                              @AuthenticationPrincipal OAuth2User principal) {
        String email = getEmailFromPrincipal(principal);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Only creator can approve/deny
        if (!trip.getCreator().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: Only trip creator can approve/deny");
        }

        if ("approve".equalsIgnoreCase(action)) {
            trip.setStatus("Approved");
        } else {
            trip.setStatus("Denied");
        }

        return tripRepository.save(trip);
    }

    @PutMapping("{id}/update")
    public Trip updateTrip(@PathVariable Long id, @RequestBody Trip updatedTrip,
                           @AuthenticationPrincipal OAuth2User principal) {
        String email = getEmailFromPrincipal(principal);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Only creator can update
        if (!trip.getCreator().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: Only trip creator can update");
        }

        // Update fields
        trip.setDestination(updatedTrip.getDestination());
        trip.setBudget(updatedTrip.getBudget());
        trip.setStartDate(updatedTrip.getStartDate());
        trip.setEndDate(updatedTrip.getEndDate());
        trip.setFemaleAllowed(updatedTrip.getFemaleAllowed());
        trip.setMaleCount(updatedTrip.getMaleCount());
        trip.setFemaleCount(updatedTrip.getFemaleCount());

        return tripRepository.save(trip);
    }

    // Helper method to extract email from OAuth2User (works for both Google and GitHub)
    private String getEmailFromPrincipal(OAuth2User principal) {
        String email = principal.getAttribute("email");

        // If email is null (GitHub sometimes doesn't provide email), fallback to login
        if (email == null || email.isEmpty()) {
            String login = principal.getAttribute("login");
            if (login != null) {
                email = login + "@github.local"; // GitHub fallback
            }
        }

        if (email == null) {
            throw new RuntimeException("Could not extract email from OAuth2 principal");
        }

        return email;
    }

    // Get single trip details (for details page)
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return ResponseEntity.ok(trip);
    }

    // Generate itinerary
    @PostMapping("/{id}/generate-itinerary")
    public ResponseEntity<String> generateItinerary(
            @PathVariable Long id,
            @RequestBody(required = false) String userPrompt,
            @AuthenticationPrincipal OAuth2User principal) {

        String email = getEmailFromPrincipal(principal);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Authorization: Only creator can generate itinerary
        if (!trip.getCreator().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        // Build comprehensive trip details for AI
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        long days = (trip.getEndDate().getTime() - trip.getStartDate().getTime()) / (1000 * 60 * 60 * 24) + 1;

        String tripDetails = String.format(
                "Create a %d-day itinerary for:\n" +
                        "Destination: %s\n" +
                        "Dates: %s to %s (%d days)\n" +
                        "Budget: ₹%.2f per person\n" +
                        "Group: %d males, %d females\n" +
                        "Additional preferences: %s",
                days,
                trip.getDestination(),
                dateFormat.format(trip.getStartDate()),
                dateFormat.format(trip.getEndDate()),
                days,
                trip.getBudget(),
                trip.getMaleCount(),
                trip.getFemaleCount(),
                userPrompt != null ? userPrompt : "Standard sightseeing and local experiences"
        );

        // Generate using AI
        String generatedItinerary = itineraryService.generateItinerary(tripDetails);

        // Save to database
        trip.setItinerary(generatedItinerary);
        tripRepository.save(trip);

        return ResponseEntity.ok(generatedItinerary);
    }

}
