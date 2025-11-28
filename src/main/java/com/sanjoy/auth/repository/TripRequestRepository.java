package com.sanjoy.auth.repository;

import com.sanjoy.auth.model.TripRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {

    // Find all pending requests for trips created by a specific user
    @Query("SELECT tr FROM TripRequest tr WHERE tr.trip.creator.id = :creatorId AND tr.status = 'PENDING'")
    List<TripRequest> findPendingRequestsByCreatorId(Long creatorId);

    // Check if user already requested to join a trip
    Optional<TripRequest> findByTripIdAndUserId(Long tripId, Long userId);

    // Get all requests for a specific trip
    List<TripRequest> findByTripId(Long tripId);
}
