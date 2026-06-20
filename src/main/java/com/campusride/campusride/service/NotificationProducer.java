package com.campusride.campusride.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final String TOPIC = "booking-notifications";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendBookingNotification(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("📤 Notification sent to Kafka: " + message);
    }
}