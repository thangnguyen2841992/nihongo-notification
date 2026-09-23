package com.thang.nihongo.notification_service.wallet;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:mail_time;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.properties.hibernate.jdbc.time_zone=UTC",
    "spring.cloud.discovery.enabled=false", "eureka.client.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = WalletMailInboxTest.Config.class)
class WalletMailInboxTest {
    @TestConfiguration
    @EntityScan(basePackageClasses = WalletMailInbox.class)
    @EnableJpaRepositories(basePackageClasses = WalletMailInboxRepository.class)
    static class Config {}
    @Autowired WalletMailInboxRepository inbox;
    @Autowired EntityManager entityManager;

    @Test void enqueueUsesApplicationInstantEvenWithDatabaseAtUtcPlusSeven() {
        entityManager.createNativeQuery("SET TIME ZONE '+07:00'").executeUpdate();
        Instant now = Instant.parse("2026-09-23T01:32:00Z");
        String id = UUID.randomUUID().toString();
        inbox.enqueueOnce(id, 42L, "{}", now);
        entityManager.flush(); entityManager.clear();
        var row = inbox.findById(id).orElseThrow();
        assertEquals(now, row.getReceivedAt());
        assertEquals(now, row.getNextAttemptAt());
        assertEquals(1, inbox.lockDue(now, PageRequest.of(0, 10)).size());
        inbox.enqueueOnce(UUID.randomUUID().toString(), 42L, "{}", now.plusSeconds(60));
        entityManager.flush(); entityManager.clear();
        assertEquals(1, inbox.count());
        assertEquals(now, inbox.findById(id).orElseThrow().getNextAttemptAt());
    }
}