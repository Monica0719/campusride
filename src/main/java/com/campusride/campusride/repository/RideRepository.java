package com.campusride.campusride.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusride.campusride.enums.RideStatus;
import com.campusride.campusride.model.Ride;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);

    List<Ride> findByStatus(RideStatus status);
@Query("""
    SELECT r FROM Ride r
    WHERE r.availableSeats > 0
    AND r.status = 'SCHEDULED'
    AND r.departureTime BETWEEN :startTime AND :endTime
    AND (
        (6371 * acos(
            cos(radians(:pickupLat)) * cos(radians(r.fromLatitude)) *
            cos(radians(r.fromLongitude) - radians(:pickupLng)) +
            sin(radians(:pickupLat)) * sin(radians(r.fromLatitude))
        ))
        +
        (6371 * acos(
            cos(radians(:pickupLat)) * cos(radians(r.toLatitude)) *
            cos(radians(r.toLongitude) - radians(:pickupLng)) +
            sin(radians(:pickupLat)) * sin(radians(r.toLatitude))
        ))
        -
        (6371 * acos(
            cos(radians(r.fromLatitude)) * cos(radians(r.toLatitude)) *
            cos(radians(r.toLongitude) - radians(r.fromLongitude)) +
            sin(radians(r.fromLatitude)) * sin(radians(r.toLatitude))
        ))
    ) <= :radius
    AND (
        (6371 * acos(
            cos(radians(:dropLat)) * cos(radians(r.fromLatitude)) *
            cos(radians(r.fromLongitude) - radians(:dropLng)) +
            sin(radians(:dropLat)) * sin(radians(r.fromLatitude))
        ))
        +
        (6371 * acos(
            cos(radians(:dropLat)) * cos(radians(r.toLatitude)) *
            cos(radians(r.toLongitude) - radians(:dropLng)) +
            sin(radians(:dropLat)) * sin(radians(r.toLatitude))
        ))
        -
        (6371 * acos(
            cos(radians(r.fromLatitude)) * cos(radians(r.toLatitude)) *
            cos(radians(r.toLongitude) - radians(r.fromLongitude)) +
            sin(radians(r.fromLatitude)) * sin(radians(r.toLatitude))
        ))
    ) <= :radius
    AND (
        ((:dropLng - r.fromLongitude) * (r.toLongitude - r.fromLongitude) +
         (:dropLat - r.fromLatitude) * (r.toLatitude - r.fromLatitude))
        >
        ((:pickupLng - r.fromLongitude) * (r.toLongitude - r.fromLongitude) +
         (:pickupLat - r.fromLatitude) * (r.toLatitude - r.fromLatitude))
    )
    ORDER BY r.departureTime ASC
""")
List<Ride> findRidesOnRoute(
    @Param("pickupLat") Double pickupLat,
    @Param("pickupLng") Double pickupLng,
    @Param("dropLat") Double dropLat,
    @Param("dropLng") Double dropLng,
    @Param("radius") Double radius,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime
);
}