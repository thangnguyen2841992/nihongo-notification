package com.thang.nihongo.notification_service.wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
public interface WalletMailInboxRepository extends JpaRepository<WalletMailInbox, String> {
    @Modifying
    @Query(value = "INSERT INTO wallet_mail_inbox (event_id, deposit_id, payload, received_at, next_attempt_at, attempts) VALUES (:id, :deposit, :payload, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0) ON DUPLICATE KEY UPDATE event_id = event_id", nativeQuery = true)
    void enqueueOnce(@Param("id") String id, @Param("deposit") Long deposit, @Param("payload") String payload);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from WalletMailInbox m where m.sentAt is null and m.nextAttemptAt <= :now order by m.receivedAt, m.eventId")
    List<WalletMailInbox> lockDue(@Param("now") Instant now, Pageable page);
}
