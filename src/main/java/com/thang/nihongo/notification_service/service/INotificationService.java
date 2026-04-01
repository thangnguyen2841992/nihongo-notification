package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageResponseUser;
import jakarta.mail.MessagingException;

public interface INotificationService {
    void sendEmailActive(MessageResponseUser messageResponseUser) throws MessagingException;
}
