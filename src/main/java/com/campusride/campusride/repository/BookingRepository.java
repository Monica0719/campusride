package com.campusride.campusride.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campusride.campusride.enums.BookingStatus;
import com.campusride.campusride.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRiderId(Long riderId);

    List<Booking> findByRideId(Long rideId);

    List<Booking> findByRiderIdAndStatus(Long riderId, BookingStatus status);

    Boolean existsByRideIdAndRiderId(Long rideId, Long riderId);

    Integer countByRideIdAndStatus(Long rideId, BookingStatus status);
}