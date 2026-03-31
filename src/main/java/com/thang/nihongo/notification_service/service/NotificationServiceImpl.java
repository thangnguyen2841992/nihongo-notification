package com.thang.nihongo.notification_service.service;

import com.thang.nihongo.notification_service.model.MessageSendActiveUser;
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
    public void sendEmailActive(MessageSendActiveUser msg) {
        try {
            String url = frontendUrl + "/active/" + msg.getToUserId() + "/" + msg.getActiveCode();

            Context context = new Context();
            context.setVariable("name", msg.getToUserFullName());
            context.setVariable("url", url);

            String html = templateEngine.process("active-account", context);

            sendMail(msg.getToUserEmail(), "Kích hoạt tài khoản", html);

        } catch (Exception e) {
            log.error("Send mail failed", e);
        }
    }

    private void sendMail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        javaMailSender.send(message);
    }
}
