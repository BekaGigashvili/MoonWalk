package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class EmailServiceUnitTest {

    private EmailService emailService;
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
    }

    @Test
    void sendVerificationEmail_sendsEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String to = "user@example.com";
        String token = "12345";

        emailService.sendVerificationEmail(to, token);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendOrderDetailsEmail_sendsEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String to = "user@example.com";
        String hotelName = "Moon Hotel";
        String roomNumbers = "101, 102";
        String seatNumbers = "A1, A2";
        String returnSeatNumbers = "B1, B2";

        emailService.sendOrderDetailsEmail(to, hotelName, roomNumbers, seatNumbers, returnSeatNumbers);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}
