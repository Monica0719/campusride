package com.campusride.campusride.service;

import com.campusride.campusride.model.Booking;
import com.campusride.campusride.model.Ride;
import com.campusride.campusride.model.User;
import com.campusride.campusride.enums.BookingStatus;
import com.campusride.campusride.enums.RideStatus;
import com.campusride.campusride.repository.BookingRepository;
import com.campusride.campusride.repository.RideRepository;
import com.campusride.campusride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Redis key pattern for ride seats
    private String getSeatKey(Long rideId) {
        return "ride:seats:" + rideId;
    }

    // Initialize seats in Redis when ride is created
    public void initializeSeatsInRedis(Long rideId, int totalSeats) {
        String key = getSeatKey(rideId);
        redisTemplate.opsForValue().set(
            key,
            String.valueOf(totalSeats),
            Duration.ofHours(24)
        );
    }

    // Get available seats from Redis
    public Integer getAvailableSeatsFromRedis(Long rideId) {
        String key = getSeatKey(rideId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return Integer.parseInt(value);
    }

    // Create a new booking
    public Booking createBooking(Booking booking,
                                  Long rideId,
                                  Long riderId) {
        // Find the ride
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() ->
                new RuntimeException("Ride not found!"));

        // Find the rider
        User rider = userRepository.findById(riderId)
            .orElseThrow(() ->
                new RuntimeException("Rider not found!"));

        // Check ride is still scheduled
        if (!ride.getStatus().equals(RideStatus.SCHEDULED)) {
            throw new RuntimeException("Ride is not available!");
        }

        // Check rider hasn't already booked this ride
        if (bookingRepository.existsByRideIdAndRiderId(
                rideId, riderId)) {
            throw new RuntimeException(
                "You already booked this ride!");
        }

        // Check seats using Redis (fast & atomic!)
        String seatKey = getSeatKey(rideId);
        String cachedSeats = redisTemplate
            .opsForValue().get(seatKey);

        if (cachedSeats != null) {
            // Redis has seat count — use atomic decrement
            Long remainingSeats = redisTemplate
                .opsForValue().decrement(seatKey);

            if (remainingSeats < 0) {
                // No seats left — increment back
                redisTemplate.opsForValue().increment(seatKey);
                throw new RuntimeException(
                    "No seats available!");
            }
        } else {
            // Redis doesn't have it — check database
            if (ride.getAvailableSeats() <= 0) {
                throw new RuntimeException(
                    "No seats available!");
            }
        }

        // Set booking details
        booking.setRide(ride);
        booking.setRider(rider);
        booking.setStatus(BookingStatus.PENDING);
        booking.setFarePaid(ride.getFarePerSeat());

        // Decrease available seats in database
        ride.setAvailableSeats(ride.getAvailableSeats() - 1);
        rideRepository.save(ride);

        return bookingRepository.save(booking);
    }

    // Get all bookings by rider
    public List<Booking> getBookingsByRider(Long riderId) {
        return bookingRepository.findByRiderId(riderId);
    }

    // Get all bookings for a ride
    public List<Booking> getBookingsByRide(Long rideId) {
        return bookingRepository.findByRideId(rideId);
    }

    // Get booking by id
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // Cancel a booking
    public Booking cancelBooking(Long bookingId, Long riderId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() ->
                new RuntimeException("Booking not found!"));

        // Only rider who booked can cancel
        if (!booking.getRider().getId().equals(riderId)) {
            throw new RuntimeException(
                "You can only cancel your own booking!");
        }

        // Give seat back in database
        Ride ride = booking.getRide();
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);
        rideRepository.save(ride);

        // Give seat back in Redis too!
        String seatKey = getSeatKey(ride.getId());
        if (redisTemplate.hasKey(seatKey)) {
            redisTemplate.opsForValue().increment(seatKey);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    // Confirm a booking (by driver)
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() ->
                new RuntimeException("Booking not found!"));
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }
}