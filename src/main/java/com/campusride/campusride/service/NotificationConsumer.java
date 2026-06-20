package com.campusride.campusride.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @KafkaListener(
        topics = "booking-notifications",
        groupId = "campusride-group"
    )
    public void listen(String message) {
        System.out.println("📥 Notification received: " + message);
        // In future: send actual SMS/Push/Email here
        processNotification(message);
    }

    private void processNotification(String message) {
        System.out.println(
            "🔔 Processing notification: " + message
        );
        System.out.println(
            "✅ Driver would be notified now!"
        );
    }
}