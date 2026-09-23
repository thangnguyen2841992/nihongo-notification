package com.thang.nihongo.notification_service.wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihongo.events.WalletEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class WalletMailConsumer {
    private final ObjectMapper mapper;
    private final WalletMailInboxRepository inbox;
    @KafkaListener(topics = "${wallet.events.topic:wallet.events.v1}", groupId = "wallet-success-email-v1",
        containerFactory = "walletMailKafkaFactory", autoStartup = "${wallet.mail.enabled:true}")
    @Transactional(rollbackFor = Exception.class)
    public void receive(String payload) throws Exception {
        WalletEvent event = mapper.readValue(payload, WalletEvent.class);
        if ("APPROVED".equals(event.type()) || "REJECTED".equals(event.type())) inbox.enqueueOnce(event.eventId(), event.depositId(), payload, java.time.Instant.now());
        // Offset is committed only after the durable inbox transaction returns successfully.
    }
}
