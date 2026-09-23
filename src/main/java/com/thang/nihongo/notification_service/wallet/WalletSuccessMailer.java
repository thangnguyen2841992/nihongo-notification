package com.thang.nihongo.notification_service.wallet;
import com.nihongo.events.WalletEvent;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
@Service
public class WalletSuccessMailer {
    private final JavaMailSender sender;
    private final TemplateEngine templates;
    private final String from;
    private final String frontend;
    public WalletSuccessMailer(JavaMailSender sender, TemplateEngine templates,
        @Value("${spring.mail.username}") String from, @Value("${app.frontend.url}") String frontend) {
        this.sender = sender; this.templates = templates; this.from = from; this.frontend = frontend;
    }
    public void send(WalletEvent event) throws MessagingException {
        boolean approved = "APPROVED".equals(event.type());
        if (!approved && !"REJECTED".equals(event.type())) throw new IllegalArgumentException("Only final deposit results have receipts");
        NumberFormat money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
        Context context = new Context();
        context.setVariable("reference", "NAP" + event.depositId());
        context.setVariable("amount", money.format(event.amount()));
        context.setVariable("balance", money.format(event.balance()));
        context.setVariable("reviewNote", event.reviewNote() == null || event.reviewNote().isBlank()
            ? "Vui lòng xem chi tiết trong lịch sử nạp tiền hoặc liên hệ hỗ trợ." : event.reviewNote());
        context.setVariable("walletUrl", frontend.replaceAll("/+$", "") + "/wallet");
        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(from); helper.setTo(event.email());
        helper.setSubject((approved ? "Nạp tiền thành công – NAP" : "Nạp tiền không thành công – NAP") + event.depositId());
        helper.setText(templates.process(approved ? "wallet-deposit-success" : "wallet-deposit-failure", context), true);
        message.setHeader("X-Wallet-Event-Id", event.eventId());
        // Synchronous: the inbox must not be marked sent before SMTP reports success.
        sender.send(message);
    }
}
