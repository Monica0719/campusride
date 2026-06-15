package com.campusride.campusride.repository;

import com.campusride.campusride.model.Ride;
import com.campusride.campusride.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);

    List<Ride> findByStatus(RideStatus status);

    @Query("""
        SELECT r FROM Ride r
        WHERE r.toLocation = :toLocation
        AND r.availableSeats > 0
        AND r.status = 'SCHEDULED'
        AND r.departureTime BETWEEN :startTime AND :endTime
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(r.fromLatitude)) *
                cos(radians(r.fromLongitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(r.fromLatitude))
            )
        ) <= :radius
        ORDER BY r.departureTime ASC
    """)
    List<Ride> findNearbyRides(
        @Param("toLocation") String toLocation,
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radius") Double radius,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}