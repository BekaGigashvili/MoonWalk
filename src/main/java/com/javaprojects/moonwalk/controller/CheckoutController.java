package com.javaprojects.moonwalk.controller;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.order.CheckoutRequest;
import com.javaprojects.moonwalk.model.order.StripeResponse;
import com.javaprojects.moonwalk.service.StripeService;
import com.javaprojects.moonwalk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class CheckoutController {
    private final StripeService stripeService;
    private final UserService userService;

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest checkoutRequest
    ) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        StripeResponse response = stripeService.checkout(
                user,
                checkoutRequest.getTripBookingRequest(),
                checkoutRequest.getRoomBookingRequest(),
                checkoutRequest.getStripeRequest()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
