package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class EmailTemplateTest {

    @Autowired
    private TemplateEngine templateEngine;

    private User testUser;
    private LocalDateTime testDateTime;
    private String formattedDateTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirstName("Juan");
        testUser.setLastName("Pérez");
        testUser.setEmail("juan.perez@example.com");

        testDateTime = LocalDateTime.now();
        formattedDateTime = testDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Test
    void passwordChangedEmailTemplate_ShouldRenderCorrectly() {
        // Arrange
        Context context = new Context();
        context.setVariable("user", testUser);
        context.setVariable("changeDate", formattedDateTime);
        context.setVariable("loginUrl", "http://localhost:8080/login");

        // Act
        String content = templateEngine.process("password-changed-email", context);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Juan"));
        assertTrue(content.contains("contraseña ha sido cambiada"));
        assertTrue(content.contains(formattedDateTime));
        assertTrue(content.contains("http://localhost:8080/login"));
    }

    @Test
    void roleChangedEmailTemplate_ShouldRenderCorrectly() {
        // Arrange
        Context context = new Context();
        context.setVariable("user", testUser);
        context.setVariable("changeDate", formattedDateTime);
        context.setVariable("roles", Set.of("MEMBER", "LIBRARIAN"));

        // Act
        String content = templateEngine.process("role-changed-email", context);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Juan"));
        assertTrue(content.contains("roles han sido actualizados"));
        assertTrue(content.contains(formattedDateTime));
        assertTrue(content.contains("MEMBER"));
        assertTrue(content.contains("LIBRARIAN"));
    }

    @Test
    void accountEnabledEmailTemplate_ShouldRenderCorrectly() {
        // Arrange
        Context context = new Context();
        context.setVariable("user", testUser);
        context.setVariable("enableDate", formattedDateTime);
        context.setVariable("loginUrl", "http://localhost:8080/login");

        // Act
        String content = templateEngine.process("account-enabled-email", context);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Juan"));
        assertTrue(content.contains("cuenta ha sido activada"));
        assertTrue(content.contains(formattedDateTime));
        assertTrue(content.contains("http://localhost:8080/login"));
    }

    @Test
    void accountDisabledEmailTemplate_ShouldRenderCorrectly() {
        // Arrange
        String reason = "Inactividad prolongada";
        Context context = new Context();
        context.setVariable("user", testUser);
        context.setVariable("disableDate", formattedDateTime);
        context.setVariable("reason", reason);

        // Act
        String content = templateEngine.process("account-disabled-email", context);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Juan"));
        assertTrue(content.contains("cuenta ha sido desactivada"));
        assertTrue(content.contains(formattedDateTime));
        assertTrue(content.contains(reason));
    }

    @Test
    void accountDeletedEmailTemplate_ShouldRenderCorrectly() {
        // Arrange
        Context context = new Context();
        context.setVariable("user", testUser);
        context.setVariable("deleteDate", formattedDateTime);

        // Act
        String content = templateEngine.process("account-deleted-email", context);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Juan"));
        assertTrue(content.contains("cuenta ha sido eliminada"));
        assertTrue(content.contains(formattedDateTime));
        assertTrue(content.contains("datos han sido eliminados"));
    }
}