package com.thang.nihongo.notification_service.wallet;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "wallet_mail_inbox", indexes = @Index(name = "idx_wallet_mail_due", columnList = "sent_at,next_attempt_at"))
@Getter @Setter @NoArgsConstructor
public class WalletMailInbox {
    @Id @Column(length = 36) private String eventId;
    @Column(nullable = false, unique = true) private Long depositId;
    @Lob @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Column(nullable = false) private Instant receivedAt;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "sent_at") private Instant sentAt;
    private int attempts;
}
