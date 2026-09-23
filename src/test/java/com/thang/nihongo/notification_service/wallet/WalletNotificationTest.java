package com.thang.nihongo.notification_service.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihongo.events.WalletEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletNotificationTest {
    final ObjectMapper mapper = new ObjectMapper();
    WalletEvent event(String type) {
        return new WalletEvent(1, UUID.randomUUID().toString(), type, 10L, "owner",
            "owner@example.com", new BigDecimal("10000"), new BigDecimal("15000"),
            Instant.now().toString(), "<script>alert('unsafe')</script>");
    }
    @ParameterizedTest @ValueSource(strings = {"APPROVED", "REJECTED"})
    void queuesBothFinalResults(String type) throws Exception {
        var inbox = mock(WalletMailInboxRepository.class);
        var event = event(type);
        String payload = mapper.writeValueAsString(event);
        new WalletMailConsumer(mapper, inbox).receive(payload);
        verify(inbox).enqueueOnce(eq(event.eventId()), eq(10L), eq(payload), argThat(now -> !now.isAfter(Instant.now()) && now.isAfter(Instant.now().minusSeconds(10))));
    }
    @Test void pendingDoesNotSendAnOutcomeEmail() throws Exception {
        var inbox = mock(WalletMailInboxRepository.class);
        new WalletMailConsumer(mapper, inbox).receive(mapper.writeValueAsString(event("CREATED")));
        verifyNoInteractions(inbox);
    }
    @ParameterizedTest @ValueSource(strings = {"APPROVED", "REJECTED"})
    void rendersAndSendsCorrectEmail(String type) throws Exception {
        var sender = mock(JavaMailSender.class);
        var message = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(message);
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML"); resolver.setCharacterEncoding("UTF-8");
        var templates = new SpringTemplateEngine(); templates.setTemplateResolver(resolver);
        var event = event(type);
        new WalletSuccessMailer(sender, templates, "sender@example.com", "https://example.com/").send(event);
        verify(sender).send(message);
        assertEquals("owner@example.com", message.getAllRecipients()[0].toString());
        assertTrue(message.getSubject().contains(type.equals("APPROVED") ? "thành công" : "không thành công"));
        String html = message.getContent().toString();
        assertTrue(html.contains("NAP10")); assertTrue(html.contains("https://example.com/wallet"));
        if (type.equals("REJECTED")) {
            assertTrue(html.contains("&lt;script&gt;"));
            assertFalse(html.contains("<script>"));
        }
        assertEquals(event.eventId(), message.getHeader("X-Wallet-Event-Id", null));
    }
    @Test void smtpFailureRemainsPendingForRetry() throws Exception {
        var inbox = mock(WalletMailInboxRepository.class);
        var mailer = mock(WalletSuccessMailer.class);
        var row = new WalletMailInbox();
        row.setEventId(UUID.randomUUID().toString());
        row.setPayload(mapper.writeValueAsString(event("REJECTED")));
        when(inbox.lockDue(any(), any())).thenReturn(List.of(row));
        doThrow(new MailSendException("offline")).when(mailer).send(any());
        assertTrue(new WalletMailDelivery(inbox, mapper, mailer).sendNext());
        assertNull(row.getSentAt()); assertEquals(1, row.getAttempts());
        assertTrue(row.getNextAttemptAt().isAfter(Instant.now()));
        verify(inbox).save(row);
    }
    @Test void oldVersionOnePayloadWithoutNoteIsAccepted() throws Exception {
        var tree = mapper.valueToTree(event("REJECTED"));
        ((com.fasterxml.jackson.databind.node.ObjectNode) tree).remove("reviewNote");
        assertNull(mapper.treeToValue(tree, WalletEvent.class).reviewNote());
    }
}
