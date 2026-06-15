package com.campusride.campusride.controller;

import com.campusride.campusride.model.Ride;
import com.campusride.campusride.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
            @RequestParam String toLocation,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10.0") Double radius) {
        return ResponseEntity.ok(
            rideService.searchNearbyRides(toLocation, lat, lng, radius)
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