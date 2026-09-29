package com.thang.nihongo.notification_service;

import com.thang.nihongo.notification_service.model.MessageResponseUser;
import com.thang.nihongo.notification_service.service.*;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivationMailTest {
    @Test void smtpFailureReachesKafkaInsteadOfAcknowledgingSuccessfulDelivery() {
        var sender = mock(JavaMailSender.class);
        var templates = mock(TemplateEngine.class);
        when(templates.process(eq("active-account"), any(org.thymeleaf.context.IContext.class))).thenReturn("<p>Activation</p>");
        when(sender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP unavailable")).when(sender).send(any(MimeMessage.class));
        var service = new NotificationServiceImpl(sender, templates);
        ReflectionTestUtils.setField(service, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
        var consumer = new KafkaService(service);
        var message = new MessageResponseUser("user@example.com", "user", "User", "id", "code");
        assertThrows(MailSendException.class, () -> consumer.receiveEmailActive(message));
    }
}
