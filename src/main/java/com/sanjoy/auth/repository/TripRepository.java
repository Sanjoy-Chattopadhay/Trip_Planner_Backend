package com.sanjoy.auth.repository;

import com.sanjoy.auth.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Date;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByCreatorId(Long creatorId);

    // Find upcoming trips (start date >= today)
    @Query("SELECT t FROM Trip t WHERE t.startDate >= :today ORDER BY t.startDate ASC")
    List<Trip> findUpcomingTrips(Date today);

    // Find past trips for a user (creator or member)
    @Query("SELECT t FROM Trip t WHERE t.endDate < :today AND " +
            "(t.creator.id = :userId OR :userId IN (SELECT m.id FROM t.members m)) " +
            "ORDER BY t.endDate DESC")
    List<Trip> findPastTripsByUser(Long userId, Date today);
}
