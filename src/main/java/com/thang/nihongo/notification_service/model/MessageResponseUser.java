package com.thang.nihongo.notification_service.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageResponseUser {
//    private String formEmail;

    private String toUserEmail;

    private String toUserName;
    private String toUserFullName;

    private long toUserId;
}
