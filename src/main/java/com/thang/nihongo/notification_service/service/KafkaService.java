package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageResponseUser;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private final INotificationService notificationService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public KafkaService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @KafkaListener(id = "sendEmailActiveGroup", topics = "send-email-active-response")
    public void receiveEmailActive(MessageResponseUser messageResponseUser) throws MessagingException {
//        logger.info("Received email: {}", messageSendActiveUser.getToEmail());
        this.notificationService.sendEmailActive(messageResponseUser);
    }
}