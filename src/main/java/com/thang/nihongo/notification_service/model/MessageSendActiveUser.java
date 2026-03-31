package com.thang.nihongo.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageSendActiveUser {
    private String formEmail;

    private String toEmail;

    private String activeCode;

    private String toName;

    private int toUserId;
}
