package com.lacunza.biblio.system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${biblio.app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("verificationUrl", verificationUrl);
            templateModel.put("expiryDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Verificación de cuenta - Sistema de Biblioteca", "verification-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("resetUrl", resetUrl);
            templateModel.put("expiryDate", LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Recuperación de contraseña - Sistema de Biblioteca", "reset-password-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación de contraseña", e);
        }
    }

    @Async
    public void sendLoanReminderEmail(String to, String bookTitle, LocalDate dueDate) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("bookTitle", bookTitle);
            templateModel.put("dueDate", dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            sendHtmlEmail(to, "Recordatorio de devolución - Sistema de Biblioteca", "loan-reminder-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de recordatorio de préstamo", e);
        }
    }

    @Async
    public void sendFineNotificationEmail(String to, String bookTitle, double amount) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("bookTitle", bookTitle);
            templateModel.put("amount", String.format("%.2f", amount));

            sendHtmlEmail(to, "Notificación de multa - Sistema de Biblioteca", "fine-notification-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de notificación de multa", e);
        }
    }

    @Async
    public void sendReservationConfirmationEmail(String to, String bookTitle) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("bookTitle", bookTitle);

            sendHtmlEmail(to, "Confirmación de reserva - Sistema de Biblioteca", "reservation-confirmation-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación de reserva", e);
        }
    }

    @Async
    public void sendReservationAvailableEmail(String to, String bookTitle, int hoursToPickup) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("bookTitle", bookTitle);
            templateModel.put("hoursToPickup", hoursToPickup);
            templateModel.put("expiryDate", LocalDateTime.now().plusHours(hoursToPickup).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Libro disponible para retirar - Sistema de Biblioteca", "reservation-available-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de libro disponible", e);
        }
    }

    @Async
    public void sendPasswordChangedEmail(String to) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("loginUrl", frontendUrl + "/auth/login");
            templateModel.put("changeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Cambio de contraseña - Sistema de Biblioteca", "password-changed-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de cambio de contraseña", e);
        }
    }

    @Async
    public void sendRoleChangedEmail(String to, Collection<String> roles) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("roles", String.join(", ", roles));
            templateModel.put("changeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Actualización de roles - Sistema de Biblioteca", "role-changed-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de actualización de roles", e);
        }
    }

    @Async
    public void sendAccountEnabledEmail(String to) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("loginUrl", frontendUrl + "/auth/login");
            templateModel.put("enableDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Cuenta habilitada - Sistema de Biblioteca", "account-enabled-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de cuenta habilitada", e);
        }
    }

    @Async
    public void sendAccountDisabledEmail(String to, String reason) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("reason", reason);
            templateModel.put("disableDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Cuenta deshabilitada - Sistema de Biblioteca", "account-disabled-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de cuenta deshabilitada", e);
        }
    }

    @Async
    public void sendAccountDeletedEmail(String to) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("deleteDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            sendHtmlEmail(to, "Cuenta eliminada - Sistema de Biblioteca", "account-deleted-email", templateModel);
        } catch (Exception e) {
            log.error("Error al enviar email de cuenta eliminada", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariables(templateModel);
        String htmlContent = templateEngine.process(templateName, context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Email enviado a: {}", to);
    }
    
    /**
     * Envía un correo electrónico simple con contenido de texto plano
     * 
     * @param to Dirección de correo electrónico del destinatario
     * @param subject Asunto del correo electrónico
     * @param content Contenido del correo electrónico en texto plano
     */
    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            
            mailSender.send(message);
            log.info("Email de texto plano enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de texto plano", e);
        }
    }
}