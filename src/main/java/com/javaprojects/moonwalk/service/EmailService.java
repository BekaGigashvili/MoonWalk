package com.javaprojects.moonwalk.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Email verification");
        helper.setText("Please verify your email address by clicking on the link below: " +
                "http://localhost:8080/auth/verify?token=" + token);
        mailSender.send(message);
    }

    public void sendOrderDetailsEmail(
            String to,
            String hotelName,
            String roomNumbers,
            String seatNumbers,
            String returnSeatNumbers
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Order Details");
        helper.setText("Hotel: " + hotelName + "\n"
                + "Room Numbers: " + roomNumbers + "\n" + "Seat Numbers: "
                + seatNumbers + "\n" + "Return Seat Numbers: " + returnSeatNumbers);
        mailSender.send(message);
    }
}
