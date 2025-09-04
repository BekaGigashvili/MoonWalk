package com.javaprojects.moonwalk.service;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.order.Order;
import com.javaprojects.moonwalk.model.order.StripeRequest;
import com.javaprojects.moonwalk.model.order.StripeResponse;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTripBookingRequest;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeService {
    @Value("${stripe.secretKey}")
    private String secretKey;
    private final OrderService orderService;

    public StripeResponse checkout(
            User user,
            RocketTripBookingRequest tripBookingRequest,
            RoomBookingRequest roomBookingRequest,
            StripeRequest stripeRequest
    ){
        Stripe.apiKey=secretKey;

        Order order = orderService.order(
                user.getEmail(),
                tripBookingRequest,
                roomBookingRequest
                );

        Rocket rocket = order.getRocketTripBooking().getRocketTrip().getRocket();

        Rocket returnRocket = order.getRocketTripBooking().getReturnTrip().getRocket();

        List<Room> rooms = order.getRoomBooking().getRooms();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount((long) (rocket.getPrice() * tripBookingRequest.getNumPassengers() * 100)) // amount in cents
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Rocket - " + rocket.getId() + ": Earth to Moon, " + tripBookingRequest.getNumPassengers() + " passengers")
                                                .build()
                                )
                                .build()
                )
                .build();

        SessionCreateParams.LineItem lineItem2 = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount((long) (returnRocket.getPrice() * tripBookingRequest.getNumPassengers() * 100)) // amount in cents
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Rocket - " + returnRocket.getId() + ": Moon to Earth, " + tripBookingRequest.getNumPassengers() + " passengers")
                                                .build()
                                )
                                .build()
                )
                .build();


        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/main")
                .setCancelUrl("http://localhost:3000/main")
                .putMetadata("userId", String.valueOf(user.getId()))
                .putMetadata("orderId", String.valueOf(order.getId()))
                .putMetadata("tripBookingId", String.valueOf(order.getRocketTripBooking().getId()))
                .putMetadata("roomBookingId", String.valueOf(order.getRoomBooking().getId()))
                .putMetadata("paymentMethod", stripeRequest.getPaymentMethodType().toString())
                .addLineItem(lineItem)
                .addLineItem(lineItem2);

        LocalDateTime checkin = order.getRoomBooking().getCheckInDate();
        LocalDateTime checkout = order.getRoomBooking().getCheckOutDate();

        long days = ChronoUnit.DAYS
                .between(checkin.toLocalDate(), checkout.toLocalDate());

        for (Room room : rooms) {
            SessionCreateParams.LineItem roomLineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount((long) (room.getPrice() * 100 * days))
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName("Room: " + room.getRoomNumber() + ", Capacity: " + room.getCapacity() + " for " + days + " days")
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            paramsBuilder.addLineItem(roomLineItem);
        }

        SessionCreateParams params = paramsBuilder.build();

        Session session;
        try{
            session = Session.create(params);
            return StripeResponse.builder()
                    .status("success")
                    .message("Payment session created")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        }catch (StripeException e){
            System.out.println(e.getMessage());
            return StripeResponse.builder()
                    .status("error")
                    .message(e.getMessage())
                    .build();
        }
    }
}

