package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageResponseUser;
import jakarta.mail.MessagingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private final INotificationService notificationService;


    public KafkaService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @KafkaListener(id = "sendEmailActiveGroup", topics = "send-email-active-response", containerFactory = "activationKafkaFactory")
    public void receiveEmailActive(MessageResponseUser messageResponseUser) throws MessagingException {
        this.notificationService.sendEmailActive(messageResponseUser);
    }
}
