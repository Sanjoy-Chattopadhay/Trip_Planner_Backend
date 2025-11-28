package com.sanjoy.auth.controller;

import com.sanjoy.auth.dto.TripRequestDTO;
import com.sanjoy.auth.model.*;
import com.sanjoy.auth.dto.*;
import com.sanjoy.auth.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripRequestRepository tripRequestRepository;

    public TripController(TripRepository tripRepository,
                          UserRepository userRepository,
                          TripRequestRepository tripRequestRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.tripRequestRepository = tripRequestRepository;
    }

    // ====== ADD THESE NEW ENDPOINTS ======

    // Get user's own created trips
    @GetMapping("/my")
    public List<Map<String, Object>> getMyTrips(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUserFromPrincipal(principal);
        List<Trip> trips = tripRepository.findByCreatorId(user.getId());
        return trips.stream().map(this::tripToMap).collect(Collectors.toList());
    }

    // Create a new trip
    @PostMapping("/create")
    public Map<String, Object> createTrip(@RequestBody TripCreateRequest request,
                                          @AuthenticationPrincipal OAuth2User principal) {
        User user = getUserFromPrincipal(principal);

        Trip trip = new Trip();
        trip.setDestination(request.getDestination());
        trip.setBudget(request.getBudget());
        trip.setStartDate(parseDate(request.getStartDate()));
        trip.setEndDate(parseDate(request.getEndDate()));
        trip.setFemaleAllowed(request.getFemaleAllowed() != null ? request.getFemaleAllowed() : true);
        trip.setMaleCount(request.getMaleCount() != null ? request.getMaleCount() : 0);
        trip.setFemaleCount(request.getFemaleCount() != null ? request.getFemaleCount() : 0);
        trip.setStatus("upcoming");
        trip.setCreator(user);

        Trip savedTrip = tripRepository.save(trip);
        return tripToMap(savedTrip);
    }

    // Update existing trip
    @PutMapping("/{id}/update")
    public Map<String, Object> updateTrip(@PathVariable Long id,
                                          @RequestBody TripCreateRequest request,
                                          @AuthenticationPrincipal OAuth2User principal) {
        User user = getUserFromPrincipal(principal);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Verify user is the creator
        if (!trip.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to edit this trip");
        }

        trip.setDestination(request.getDestination());
        trip.setBudget(request.getBudget());
        trip.setStartDate(parseDate(request.getStartDate()));
        trip.setEndDate(parseDate(request.getEndDate()));
        trip.setFemaleAllowed(request.getFemaleAllowed() != null ? request.getFemaleAllowed() : true);
        trip.setMaleCount(request.getMaleCount() != null ? request.getMaleCount() : 0);
        trip.setFemaleCount(request.getFemaleCount() != null ? request.getFemaleCount() : 0);

        Trip updatedTrip = tripRepository.save(trip);
        return tripToMap(updatedTrip);
    }


    // Get single trip details
    @GetMapping("/{id}")
    public Map<String, Object> getTripById(@PathVariable Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return tripToMap(trip);
    }

    // Generate itinerary endpoint (if you're using AI itinerary generation)
    @PostMapping("/{id}/generate-itinerary")
    public String generateItinerary(@PathVariable Long id,
                                    @RequestBody String userPreferences,
                                    @AuthenticationPrincipal OAuth2User principal) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // For now, return a placeholder. You can integrate AI service here
        String itinerary = "Day-by-day itinerary for " + trip.getDestination() +
                "\n\nUser preferences: " + userPreferences +
                "\n\n(Implement AI generation service here)";

        trip.setItinerary(itinerary);
        tripRepository.save(trip);

        return itinerary;
    }

    // ====== EXISTING ENDPOINTS ======

    // Get all upcoming trips
    @GetMapping("/upcoming")
    public List<Map<String, Object>> getUpcomingTrips() {
        List<Trip> trips = tripRepository.findUpcomingTrips(new Date());
        return trips.stream().map(this::tripToMap).collect(Collectors.toList());
    }

    // Get user's past trips
    @GetMapping("/past")
    public List<Map<String, Object>> getPastTrips(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUserFromPrincipal(principal);
        List<Trip> trips = tripRepository.findPastTripsByUser(user.getId(), new Date());
        return trips.stream().map(this::tripToMap).collect(Collectors.toList());
    }

    // Request to join a trip
    @PostMapping("/{tripId}/request")
    public Map<String, String> requestToJoin(@PathVariable Long tripId,
                                             @AuthenticationPrincipal OAuth2User principal) {
        User user = getUserFromPrincipal(principal);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Check if user is the creator
        if (trip.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot request to join your own trip");
        }

        // Check if already requested
        Optional<TripRequest> existing = tripRequestRepository.findByTripIdAndUserId(tripId, user.getId());
        if (existing.isPresent()) {
            throw new RuntimeException("You have already requested to join this trip");
        }

        // Create request
        TripRequest request = new TripRequest();
        request.setTrip(trip);
        request.setUser(user);
        tripRequestRepository.save(request);

        return Map.of("message", "Request sent successfully");
    }

    // Get pending requests for trips I created
    @GetMapping("/requests/pending")
    public List<TripRequestDTO> getPendingRequests(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUserFromPrincipal(principal);
        List<TripRequest> requests = tripRequestRepository.findPendingRequestsByCreatorId(user.getId());

        return requests.stream().map(req -> {
            TripRequestDTO dto = new TripRequestDTO();
            dto.setId(req.getId());
            dto.setTripId(req.getTrip().getId());
            dto.setDestination(req.getTrip().getDestination());
            dto.setStartDate(req.getTrip().getStartDate().toString());
            dto.setEndDate(req.getTrip().getEndDate().toString());
            dto.setUserId(req.getUser().getId());
            dto.setUserName(req.getUser().getName());
            dto.setUserEmail(req.getUser().getEmail());
            dto.setUserPicture(req.getUser().getPicture());
            dto.setStatus(req.getStatus().name());
            dto.setRequestedAt(req.getRequestedAt().toString());
            return dto;
        }).collect(Collectors.toList());
    }

    // Approve request
    @PostMapping("/requests/{requestId}/approve")
    public Map<String, String> approveRequest(@PathVariable Long requestId,
                                              @AuthenticationPrincipal OAuth2User principal) {
        User creator = getUserFromPrincipal(principal);
        TripRequest request = tripRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Verify the user is the trip creator
        if (!request.getTrip().getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        tripRequestRepository.save(request);

        // Add user to trip members
        Trip trip = request.getTrip();
        if (trip.getMembers() == null) {
            trip.setMembers(new ArrayList<>());
        }
        trip.getMembers().add(request.getUser());
        tripRepository.save(trip);

        return Map.of("message", "Request approved");
    }

    // Deny request
    @PostMapping("/requests/{requestId}/deny")
    public Map<String, String> denyRequest(@PathVariable Long requestId,
                                           @AuthenticationPrincipal OAuth2User principal) {
        User creator = getUserFromPrincipal(principal);
        TripRequest request = tripRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getTrip().getCreator().getId().equals(creator.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        request.setStatus(RequestStatus.DENIED);
        tripRequestRepository.save(request);

        return Map.of("message", "Request denied");
    }

    // ====== HELPER METHODS ======

    private User getUserFromPrincipal(OAuth2User principal) {
        String email = principal.getAttribute("email");
        if (email == null) {
            email = principal.getAttribute("login") + "@github.local";
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> tripToMap(Trip trip) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", trip.getId());
        map.put("destination", trip.getDestination());
        map.put("budget", trip.getBudget());
        map.put("startDate", trip.getStartDate());
        map.put("endDate", trip.getEndDate());
        map.put("femaleAllowed", trip.getFemaleAllowed());
        map.put("maleCount", trip.getMaleCount());
        map.put("femaleCount", trip.getFemaleCount());
        map.put("status", trip.getStatus());
        map.put("itinerary", trip.getItinerary());
        map.put("creator", Map.of(
                "id", trip.getCreator().getId(),
                "name", trip.getCreator().getName(),
                "email", trip.getCreator().getEmail()
        ));
        return map;
    }

    private Date parseDate(String dateStr) {
        try {
            // Parse yyyy-MM-dd format
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + dateStr);
        }
    }
}
