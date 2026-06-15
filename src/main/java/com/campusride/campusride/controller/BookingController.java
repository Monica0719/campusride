package com.campusride.campusride.controller;

import com.campusride.campusride.model.Booking;
import com.campusride.campusride.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // POST create a booking
    @PostMapping("/{rideId}/rider/{riderId}")
    public ResponseEntity<Booking> createBooking(
            @PathVariable Long rideId,
            @PathVariable Long riderId,
            @RequestBody Booking booking) {
        try {
            Booking createdBooking = bookingService.createBooking(
                booking, rideId, riderId
            );
            return ResponseEntity.ok(createdBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET all bookings by rider
    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<Booking>> getBookingsByRider(
            @PathVariable Long riderId) {
        return ResponseEntity.ok(
            bookingService.getBookingsByRider(riderId)
        );
    }

    // GET all bookings for a ride
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<Booking>> getBookingsByRide(
            @PathVariable Long rideId) {
        return ResponseEntity.ok(
            bookingService.getBookingsByRide(rideId)
        );
    }

    // GET booking by id
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id) {
        return bookingService.getBookingById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // PUT cancel a booking
    @PutMapping("/{bookingId}/cancel/{riderId}")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId,
            @PathVariable Long riderId) {
        try {
            return ResponseEntity.ok(
                bookingService.cancelBooking(bookingId, riderId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT confirm a booking
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<Booking> confirmBooking(
            @PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}