package com.thang.nihongo.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageSendActiveUser {
    private String formEmail;

    private String toUserEmail;

    private String activeCode;

    private String toUserName;
    private String toUserFullName;

    private int toUserId;
}
