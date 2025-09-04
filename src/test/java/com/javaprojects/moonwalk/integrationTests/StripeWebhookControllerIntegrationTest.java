package com.javaprojects.moonwalk.integrationTests;

import com.javaprojects.moonwalk.model.OrderStatus;
import com.javaprojects.moonwalk.model.Role;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.model.order.Order;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTrip;
import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.OrderService;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import com.javaprojects.moonwalk.service.room.RoomService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"webhook.secretKey=test_secret"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StripeWebhookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    private String fakePayload;
    private String fakeSigHeader;

    private Long testOrderId;
    @Autowired
    private RoomBookingService roomBookingService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private RocketTripService tripService;
    @Autowired
    private RocketTripBookingService tripBookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private RocketTripReturnService tripReturnService;
    @Autowired
    private RocketService rocketService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeAll
    void setUp() {
        orderService.deleteAll();
        tripBookingService.deleteAll();
        roomBookingService.deleteAll();
        userService.deleteAll();
        tripService.deleteAll();
        tripReturnService.deleteAll();
        rocketService.deleteAll();
        roomService.deleteAll();
        hotelService.deleteAll();

        User passenger = new User();
        passenger.setEmail("passenger@example.com");
        passenger.setFirstName("Passenger");
        passenger.setLastName("Passenger");
        passenger.setPassword(passwordEncoder.encode("password"));
        passenger.setRole(Role.PASSENGER);
        userService.save(passenger);

        Rocket rocket = new Rocket();
        rocket.setPrice(1500);
        rocket.setSeatsTotal(10);
        rocket.setLaunchDay(WeekDay.MONDAY);
        rocket.setLaunchTime("12");
        rocket = rocketService.save(rocket);

        RocketTrip rocketTrip = new RocketTrip();
        rocketTrip.setRocket(rocket);
        rocketTrip.setLaunchExactTime(LocalDateTime.now().plusDays(1));
        rocketTrip = tripService.save(rocketTrip);

        RocketTripReturn rocketTripReturn = new RocketTripReturn();
        rocketTripReturn.setRocket(rocket);
        rocketTripReturn.setReturnExactTime(LocalDateTime.now().plusDays(1).plusHours(3));
        rocketTripReturn = tripReturnService.save(rocketTripReturn);

        RocketTripBooking tripBooking = new RocketTripBooking();
        tripBooking.setRocketTrip(rocketTrip);
        tripBooking.setReturnTrip(rocketTripReturn);
        tripBooking.setUser(passenger);
        tripBooking.setSeatNumbers("A1 A2");
        tripBooking.setReturnSeatNumbers("B1 B2");
        tripBooking = tripBookingService.save(tripBooking);

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setEmail("test@test.com");
        hotel.setDescription("Test hotel");
        hotel = hotelService.save(hotel);

        Room room = new Room();
        room.setRoomNumber(101);
        room.setCapacity(2);
        room.setPrice(100.0);
        room.setHotel(hotel);
        room = roomService.save(room);

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setCheckInDate(LocalDateTime.now());
        roomBooking.setCheckOutDate(LocalDateTime.now().plusDays(1));
        roomBooking.getRooms().add(room);
        roomBooking = roomBookingService.save(roomBooking);

        Order order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.valueOf(100));
        order.setRoomBooking(roomBooking);
        order.setRocketTripBooking(tripBooking);
        order = orderService.save(order);

        testOrderId = order.getId();

        fakePayload = "{ \"id\": \"evt_123\", \"type\": \"checkout.session.completed\", " +
                "\"data\": { \"object\": { \"metadata\": { \"orderId\": \"" + testOrderId + "\" } } } }";
        fakeSigHeader = "t=123,v1=fakesignature";
    }


    @Test
    void handleStripeEvent_validSignature_updatesOrderStatus() throws Exception {
        Event fakeEvent = Event.GSON.fromJson(fakePayload, Event.class);

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() ->
                            Webhook.constructEvent(fakePayload, fakeSigHeader, "test_secret"))
                    .thenReturn(fakeEvent);

            mockMvc.perform(post("/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", fakeSigHeader)
                            .content(fakePayload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook received"));

            Order updatedOrder = orderService.findById(testOrderId);
            assertEquals(OrderStatus.CONFIRMED, updatedOrder.getOrderStatus());
        }
    }

    @Test
    void handleStripeEvent_invalidSignature_returnsBadRequest() throws Exception {
        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() ->
                            Webhook.constructEvent(fakePayload, fakeSigHeader, "test_secret"))
                    .thenThrow(SignatureVerificationException.class);

            mockMvc.perform(post("/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", fakeSigHeader)
                            .content(fakePayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid signature"));
        }
    }
}
