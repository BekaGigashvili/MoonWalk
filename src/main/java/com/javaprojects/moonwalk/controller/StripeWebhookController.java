package com.javaprojects.moonwalk.controller;

import com.javaprojects.moonwalk.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class StripeWebhookController {

    private final OrderService orderService;

    @Value("${webhook.secretKey}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeEvent(HttpServletRequest request) throws IOException {
        String payload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        String sigHeader = request.getHeader("Stripe-Signature");
        System.out.println("Stripe-Signature: " + sigHeader);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(event.toJson());
                String orderIdStr = jsonNode
                        .path("data")
                        .path("object")
                        .path("metadata")
                        .path("orderId")
                        .asText();

                if (!orderIdStr.isEmpty()) {
                    Long orderId = Long.parseLong(orderIdStr);
                    orderService.confirmPayment(orderId);
                    System.out.println("Order " + orderId + " confirmed!");
                } else {
                    System.out.println("No orderId found in metadata");
                }
            } catch (Exception e) {
                System.out.println("Error parsing event JSON: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("Webhook received");
    }

}
