package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.*;
import com.javaprojects.moonwalk.model.order.*;
import com.javaprojects.moonwalk.model.rocket.*;
import com.javaprojects.moonwalk.model.room.*;
import com.javaprojects.moonwalk.service.OrderService;
import com.javaprojects.moonwalk.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StripeServiceUnitTest {

    private StripeService stripeService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        stripeService = new StripeService(orderService);

        try {
            var secretField = StripeService.class.getDeclaredField("secretKey");
            secretField.setAccessible(true);
            secretField.set(stripeService, "test_secret");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void checkout_createsStripeSessionSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        RocketTripBooking rocketTripBooking = getRocketTripBooking();

        Room room = new Room();
        room.setPrice(100.0);
        room.setRoomNumber(101);
        room.setCapacity(2);

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setRooms(Collections.singletonList(room));
        roomBooking.setCheckInDate(LocalDateTime.of(2025, 9, 4, 12, 0));
        roomBooking.setCheckOutDate(LocalDateTime.of(2025, 9, 6, 12, 0));
        roomBooking.setId(20L);

        Order order = new Order();
        order.setRocketTripBooking(rocketTripBooking);
        order.setRoomBooking(roomBooking);
        order.setNumPassengers(2);
        order.setId(99L);

        when(orderService.order(anyString(), any(), any())).thenReturn(order);

        StripeRequest stripeRequest = new StripeRequest();
        stripeRequest.setPaymentMethodType(PaymentMethodType.CARD);

        RocketTripBookingRequest tripBookingRequest = new RocketTripBookingRequest();
        tripBookingRequest.setNumPassengers(2);

        RoomBookingRequest roomBookingRequest = new RoomBookingRequest();

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getId()).thenReturn("sess_123");
            when(mockSession.getUrl()).thenReturn("http://stripe.com/session/123");
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

            StripeResponse response = stripeService.checkout(user, tripBookingRequest, roomBookingRequest, stripeRequest);

            assertEquals("success", response.getStatus());
            assertEquals("Payment session created", response.getMessage());
            assertEquals("sess_123", response.getSessionId());
            assertEquals("http://stripe.com/session/123", response.getSessionUrl());
        }
    }

    private static RocketTripBooking getRocketTripBooking() {
        Rocket rocket = new Rocket();
        rocket.setId(100L);
        rocket.setPrice(500);

        Rocket returnRocket = new Rocket();
        returnRocket.setId(101L);
        returnRocket.setPrice(400);

        RocketTrip rocketTrip = new RocketTrip();
        rocketTrip.setRocket(rocket);

        RocketTripReturn rocketTripReturn = new RocketTripReturn();
        rocketTripReturn.setRocket(returnRocket);

        RocketTripBooking rocketTripBooking = new RocketTripBooking();
        rocketTripBooking.setRocketTrip(rocketTrip);
        rocketTripBooking.setReturnTrip(rocketTripReturn);
        rocketTripBooking.setId(10L);
        return rocketTripBooking;
    }

    @Test
    void checkout_stripeException_returnsErrorResponse() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        RocketTripBooking rocketTripBooking = getRocketTripBooking();

        Room room = new Room();
        room.setPrice(100.0);
        room.setRoomNumber(101);
        room.setCapacity(2);

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setRooms(Collections.singletonList(room));
        roomBooking.setCheckInDate(LocalDateTime.of(2025, 9, 4, 12, 0));
        roomBooking.setCheckOutDate(LocalDateTime.of(2025, 9, 6, 12, 0));
        roomBooking.setId(20L);

        Order order = new Order();
        order.setRocketTripBooking(rocketTripBooking);
        order.setRoomBooking(roomBooking);
        order.setNumPassengers(2);
        order.setId(99L);

        when(orderService.order(anyString(), any(), any())).thenReturn(order);

        StripeRequest stripeRequest = new StripeRequest();
        stripeRequest.setPaymentMethodType(PaymentMethodType.CARD);

        RocketTripBookingRequest tripBookingRequest = new RocketTripBookingRequest();
        tripBookingRequest.setNumPassengers(2);

        RoomBookingRequest roomBookingRequest = new RoomBookingRequest();

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            StripeException mockException = mock(StripeException.class);
            when(mockException.getMessage()).thenReturn("Stripe failed");

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(mockException);

            StripeResponse response = stripeService.checkout(user, tripBookingRequest, roomBookingRequest, stripeRequest);

            assertEquals("error", response.getStatus());
            assertEquals("Stripe failed", response.getMessage());
            assertNull(response.getSessionId());
            assertNull(response.getSessionUrl());
        }
    }
}
