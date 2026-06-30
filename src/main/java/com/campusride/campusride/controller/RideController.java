package com.campusride.campusride.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusride.campusride.model.Ride;
import com.campusride.campusride.service.RideService;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    // POST create a ride
    @PostMapping("/{driverId}")
    public ResponseEntity<Ride> createRide(
            @PathVariable Long driverId,
            @RequestBody Ride ride) {
        try {
            Ride createdRide = rideService.createRide(ride, driverId);
            return ResponseEntity.ok(createdRide);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET ride by id
    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable Long id) {
        return rideService.getRideById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // GET all rides by driver
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getRidesByDriver(
            @PathVariable Long driverId) {
        return ResponseEntity.ok(
            rideService.getRidesByDriver(driverId)
        );
    }

    // GET search nearby rides
   @GetMapping("/search")
public ResponseEntity<List<Ride>> searchRides(
        @RequestParam Double pickupLat,
        @RequestParam Double pickupLng,
        @RequestParam Double dropLat,
        @RequestParam Double dropLng,
        @RequestParam(defaultValue = "10.0") Double radius) {
    return ResponseEntity.ok(
        rideService.searchNearbyRides(pickupLat, pickupLng, dropLat, dropLng, radius)
    );
}

    // PUT cancel a ride
    @PutMapping("/{rideId}/cancel/{driverId}")
    public ResponseEntity<Ride> cancelRide(
            @PathVariable Long rideId,
            @PathVariable Long driverId) {
        try {
            return ResponseEntity.ok(
                rideService.cancelRide(rideId, driverId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT start a ride
    @PutMapping("/{rideId}/start/{driverId}")
    public ResponseEntity<Ride> startRide(
            @PathVariable Long rideId,
            @PathVariable Long driverId) {
        try {
            return ResponseEntity.ok(
                rideService.startRide(rideId, driverId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT complete a ride
    @PutMapping("/{rideId}/complete")
    public ResponseEntity<Ride> completeRide(
            @PathVariable Long rideId) {
        try {
            return ResponseEntity.ok(
                rideService.completeRide(rideId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}