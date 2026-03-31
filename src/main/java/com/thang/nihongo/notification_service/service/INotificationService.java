package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageSendActiveUser;

public interface INotificationService {
    void sendEmailActive(MessageSendActiveUser messageSendActiveUser);
}
