package com.campusride.campusride.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.campusride.campusride.enums.BookingStatus;
import com.campusride.campusride.enums.RideStatus;
import com.campusride.campusride.model.Booking;
import com.campusride.campusride.model.Ride;
import com.campusride.campusride.model.User;
import com.campusride.campusride.repository.BookingRepository;
import com.campusride.campusride.repository.RideRepository;
import com.campusride.campusride.repository.UserRepository;

@Service
public class BookingService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationProducer notificationProducer;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String getSeatKey(Long rideId) {
        return "ride:seats:" + rideId;
    }

    public void initializeSeatsInRedis(Long rideId, int totalSeats) {
        String key = getSeatKey(rideId);
        redisTemplate.opsForValue().set(
            key,
            String.valueOf(totalSeats),
            Duration.ofHours(24)
        );
    }

    public Integer getAvailableSeatsFromRedis(Long rideId) {
        String key = getSeatKey(rideId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return Integer.parseInt(value);
    }

    public Booking createBooking(Booking booking,
                          Long rideId,
                          Long riderId) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() ->
                new RuntimeException("Ride not found!"));

        User rider = userRepository.findById(riderId)
            .orElseThrow(() ->
                new RuntimeException("Rider not found!"));

        if (!ride.getStatus().equals(RideStatus.SCHEDULED)) {
            throw new RuntimeException("Ride is not available!");
        }

        if (bookingRepository.existsByRideIdAndRiderId(
                rideId, riderId)) {
            throw new RuntimeException(
                "You already booked this ride!");
        }

        String seatKey = getSeatKey(rideId);
        String cachedSeats = redisTemplate
            .opsForValue().get(seatKey);

        if (cachedSeats != null) {
            Long remainingSeats = redisTemplate
                .opsForValue().decrement(seatKey);

            if (remainingSeats < 0) {
                redisTemplate.opsForValue().increment(seatKey);
                throw new RuntimeException(
                    "No seats available!");
            }
        } else {
            if (ride.getAvailableSeats() <= 0) {
                throw new RuntimeException(
                    "No seats available!");
            }
        }

        booking.setRide(ride);
        booking.setRider(rider);
        booking.setStatus(BookingStatus.PENDING);
        booking.setFarePaid(ride.getFarePerSeat());

        ride.setAvailableSeats(ride.getAvailableSeats() - 1);
        rideRepository.save(ride);

        Booking savedBooking = bookingRepository.save(booking);

        String message = String.format(
            "New booking! %s booked a seat on %s's ride from %s to %s",
            rider.getName(),
            ride.getDriver().getName(),
            ride.getFromLocation(),
            ride.getToLocation()
        );
        notificationProducer.sendBookingNotification(message);

        messagingTemplate.convertAndSend(
            "/topic/driver/" + ride.getDriver().getId() + "/notifications",
            message
        );

        return savedBooking;
    }

    public List<Booking> getBookingsByRider(Long riderId) {
        return bookingRepository.findByRiderId(riderId);
    }

    public List<Booking> getBookingsByRide(Long rideId) {
        return bookingRepository.findByRideId(rideId);
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking cancelBooking(Long bookingId, Long riderId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() ->
                new RuntimeException("Booking not found!"));

        if (!booking.getRider().getId().equals(riderId)) {
            throw new RuntimeException(
                "You can only cancel your own booking!");
        }

        Ride ride = booking.getRide();
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);
        rideRepository.save(ride);

        String seatKey = getSeatKey(ride.getId());
        if (redisTemplate.hasKey(seatKey)) {
            redisTemplate.opsForValue().increment(seatKey);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        String message = String.format(
            "%s cancelled their booking on your ride from %s to %s.",
            booking.getRider().getName(),
            ride.getFromLocation(),
            ride.getToLocation()
        );
        notificationProducer.sendBookingNotification(message);
        messagingTemplate.convertAndSend(
            "/topic/driver/" + ride.getDriver().getId() + "/notifications",
            message
        );

        return savedBooking;
    }

    public void cancelAllBookingsForRide(Long rideId) {
        List<Booking> bookings = bookingRepository.findByRideId(rideId);

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                String message = String.format(
                    "Sorry, %s cancelled the ride from %s to %s. Your booking was cancelled.",
                    booking.getRide().getDriver().getName(),
                    booking.getRide().getFromLocation(),
                    booking.getRide().getToLocation()
                );

                notificationProducer.sendBookingNotification(message);
                messagingTemplate.convertAndSend(
                    "/topic/rider/" + booking.getRider().getId() + "/notifications",
                    message
                );
            }
        }
    }

    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() ->
                new RuntimeException("Booking not found!"));
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        String message = String.format(
            "%s confirmed your seat on the ride from %s to %s!",
            booking.getRide().getDriver().getName(),
            booking.getRide().getFromLocation(),
            booking.getRide().getToLocation()
        );

        notificationProducer.sendBookingNotification(message);

        messagingTemplate.convertAndSend(
            "/topic/rider/" + booking.getRider().getId() + "/notifications",
            message
        );

        return savedBooking;
    }
}