package pe.com.lacunza.book.library.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@booklibrary.com");
        helper.setTo(to);
        helper.setSubject("Código de verificación - Book Library SAC");

        String htmlContent = """
            <h1>Verifica tu cuenta</h1>
            <p>Tu código de verificación es:</p>
            <h2 style="color: blue;">%s</h2>
            <p>Este código expira en 15 minutos.</p>
            """.formatted(code);

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
