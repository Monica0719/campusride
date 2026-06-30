package com.campusride.campusride.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campusride.campusride.enums.RideStatus;
import com.campusride.campusride.model.Ride;
import com.campusride.campusride.model.User;
import com.campusride.campusride.repository.RideRepository;
import com.campusride.campusride.repository.UserRepository;

@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    // Create a new ride
    public Ride createRide(Ride ride, Long driverId) {
        User driver = userRepository.findById(driverId)
            .orElseThrow(() ->
                new RuntimeException("Driver not found!"));

        ride.setDriver(driver);
        ride.setAvailableSeats(ride.getTotalSeats());
        ride.setStatus(RideStatus.SCHEDULED);

        Ride savedRide = rideRepository.save(ride);

        bookingService.initializeSeatsInRedis(
            savedRide.getId(),
            savedRide.getTotalSeats()
        );

        return savedRide;
    }

    // Get ride by id
    public Optional<Ride> getRideById(Long id) {
        return rideRepository.findById(id);
    }

    // Get all rides by driver
    public List<Ride> getRidesByDriver(Long driverId) {
        return rideRepository.findByDriverIdAndStatus(
            driverId, RideStatus.SCHEDULED
        );
    }

    // Search rides along the route corridor
    public List<Ride> searchNearbyRides(Double pickupLat,
                                        Double pickupLng,
                                        Double dropLat,
                                        Double dropLng,
                                        Double radius) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(24);
        return rideRepository.findRidesOnRoute(
            pickupLat, pickupLng, dropLat, dropLng, radius, now, endTime
        );
    }

    // Cancel a ride
    public Ride cancelRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() ->
                new RuntimeException("Ride not found!"));

        if (!ride.getDriver().getId().equals(driverId)) {
            throw new RuntimeException(
                "You can only cancel your own ride!");
        }

        ride.setStatus(RideStatus.CANCELLED);
        bookingService.cancelAllBookingsForRide(rideId);
        return rideRepository.save(ride);
    }

    // Start a ride
    public Ride startRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() ->
                new RuntimeException("Ride not found!"));

        if (!ride.getDriver().getId().equals(driverId)) {
            throw new RuntimeException(
                "You can only start your own ride!");
        }

        ride.setStatus(RideStatus.ACTIVE);
        return rideRepository.save(ride);
    }

    // Complete a ride
    public Ride completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() ->
                new RuntimeException("Ride not found!"));

        ride.setStatus(RideStatus.COMPLETED);
        return rideRepository.save(ride);
    }
}
