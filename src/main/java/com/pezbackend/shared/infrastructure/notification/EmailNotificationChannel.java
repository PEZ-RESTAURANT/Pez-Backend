package com.pezbackend.shared.infrastructure.notification;

import com.pezbackend.shared.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.springframework.scheduling.annotation.Async;
import java.util.Map;

/**
 * Canal de notificación concreto para envío de correos electrónicos formateados con Thymeleaf.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void send(String recipient, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            Context context = new Context();
            if (templateModel != null) {
                context.setVariables(templateModel);
            }
            String htmlContent = templateEngine.process(templateName, context);
            emailService.sendEmail(recipient, subject, htmlContent);
            log.info("EmailNotificationChannel: Notification email successfully sent to {}", recipient);
        } catch (Exception e) {
            log.error("EmailNotificationChannel: Failed to process and send email notification to {}: {}", recipient, e.getMessage(), e);
        }
    }
}
