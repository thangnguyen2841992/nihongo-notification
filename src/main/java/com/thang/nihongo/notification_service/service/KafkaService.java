package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageSendActiveUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void receiveEmailActive(MessageSendActiveUser messageSendActiveUser) {
        logger.info("Received email: {}", messageSendActiveUser.getToEmail());
        this.notificationService.sendEmailActive(messageSendActiveUser);
    }
}