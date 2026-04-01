package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageResponseUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class NotificationServiceImpl implements INotificationService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public NotificationServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmailActive(MessageResponseUser msg) throws MessagingException {
        String subject = "Kích hoạt tài khoản của bạn tại Japanese App";

        String url = frontendUrl
                + "/active?userId=" + msg.getToUserId()
                + "&activeCode=" + msg.getActiveCode();

        Context context = new Context();
        context.setVariable("name", msg.getToUserFullName());
        context.setVariable("url", url);

        String html = templateEngine.process("active-account", context);

        sendMail(msg.getToUserEmail(), subject, html);
    }

    private void sendMail(String toEmail, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setText(text, true);
            helper.setSubject(subject);
        } catch (MessagingException e) {
            log.error("Send mail failed to {}", toEmail, e);
            throw e;
        }
        this.javaMailSender.send(message);
    }
}
