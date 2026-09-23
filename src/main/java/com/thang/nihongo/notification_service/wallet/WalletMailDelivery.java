package com.thang.nihongo.notification_service.wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihongo.events.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
@Service @RequiredArgsConstructor @Slf4j
public class WalletMailDelivery {
    private final WalletMailInboxRepository inbox;
    private final ObjectMapper mapper;
    private final WalletSuccessMailer mailer;
    @Transactional(timeout = 45)
    public boolean sendNext() {
        var rows = inbox.lockDue(Instant.now(), PageRequest.of(0, 1));
        if (rows.isEmpty()) return false;
        WalletMailInbox row = rows.get(0);
        try {
            mailer.send(mapper.readValue(row.getPayload(), WalletEvent.class));
            row.setSentAt(Instant.now());
        } catch (Exception e) {
            row.setAttempts(row.getAttempts() + 1);
            row.setNextAttemptAt(Instant.now().plusSeconds(Math.min(3600, 5L << Math.min(row.getAttempts(), 10))));
            log.warn("Wallet receipt {} pending after attempt {}; failureType={}", row.getEventId(), row.getAttempts(), e.getClass().getSimpleName());
        }
        inbox.save(row);
        return true;
    }
}
