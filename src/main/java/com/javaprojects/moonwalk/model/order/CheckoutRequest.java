package com.javaprojects.moonwalk.model.order;

import com.javaprojects.moonwalk.model.rocket.RocketTripBookingRequest;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    private RocketTripBookingRequest tripBookingRequest;
    private RoomBookingRequest roomBookingRequest;
    private StripeRequest stripeRequest;
}
