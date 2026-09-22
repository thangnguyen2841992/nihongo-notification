package com.thang.nihongo.notification_service.wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @Slf4j
@ConditionalOnProperty(name = "wallet.mail.enabled", havingValue = "true", matchIfMissing = true)
public class WalletMailJob {
    private final WalletMailDelivery delivery;
    @Scheduled(fixedDelayString = "${wallet.mail.retry-delay-ms:2000}")
    public void run() {
        try { for (int i = 0; i < 10; i++) if (!delivery.sendNext()) break; }
        catch (RuntimeException e) { log.warn("Wallet mail queue unavailable; retrying on next scheduled run"); }
    }
}
