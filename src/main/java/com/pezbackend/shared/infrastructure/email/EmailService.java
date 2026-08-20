package com.pezbackend.shared.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements CommandLineRunner {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.from:noreply@altoque.pe}")
    private String fromEmail;

    @Override
    public void run(String... args) throws Exception {
        if (smtpUsername == null || smtpUsername.isBlank() || smtpHost == null || smtpHost.isBlank() || "localhost".equalsIgnoreCase(smtpHost)) {
            log.warn("\n" +
                    "********************************************************************************\n" +
                    "⚠️  ALERTA: CONFIGURACIÓN DE CORREO (SMTP) INCOMPLETA O EN MODO DUMMY          ⚠️\n" +
                    "--------------------------------------------------------------------------------\n" +
                    "El servidor SMTP no está configurado (spring.mail.username / host vacíos).\n" +
                    "Las notificaciones de recuperación de contraseña no se enviarán a buzones reales.\n" +
                    "En su lugar, los correos se imprimirán en los logs de la consola del servidor.\n" +
                    "Para activar el envío real, configure las siguientes variables de entorno:\n" +
                    " - SMTP_HOST\n" +
                    " - SMTP_PORT\n" +
                    " - SMTP_USERNAME\n" +
                    " - SMTP_PASSWORD\n" +
                    " - SMTP_FROM_EMAIL\n" +
                    "********************************************************************************");
        } else {
            log.info("📧 SMTP configurado exitosamente. Servidor: {}, Remitente: {}", smtpHost, fromEmail);
        }
    }

    public void sendEmail(String to, String subject, String contentHtml) {
        if (smtpUsername == null || smtpUsername.isBlank() || smtpHost == null || smtpHost.isBlank() || "localhost".equalsIgnoreCase(smtpHost)) {
            log.info("\n" +
                    "================================================================================\n" +
                    "📧 [EMAIL DUMMY LOG] Enviando correo simulado:\n" +
                    "Para: {}\n" +
                    "Asunto: {}\n" +
                    "Contenido:\n" +
                    "{}\n" +
                    "================================================================================", to, subject, contentHtml);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHtml, true);
            
            mailSender.send(message);
            log.info("📧 Correo electrónico enviado con éxito a {}", to);
        } catch (Exception e) {
            log.error("❌ Error enviando correo real a {}: {}", to, e.getMessage(), e);
            log.info("📧 [FALLBACK DUMMY LOG] Fallback impreso en consola para {}:\n{}", to, contentHtml);
        }
    }
}
