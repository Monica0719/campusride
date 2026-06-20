package com.campusride.campusride.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class LocationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Driver sends location update here
    @MessageMapping("/ride/{rideId}/location")
    public void updateLocation(
            @DestinationVariable Long rideId,
            LocationUpdate location) {

        // Broadcast location to all riders watching this ride
        messagingTemplate.convertAndSend(
            "/topic/ride/" + rideId + "/location",
            location
        );
    }

    // Simple class to hold location data
    public static class LocationUpdate {
        private Double latitude;
        private Double longitude;
        private String timestamp;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}