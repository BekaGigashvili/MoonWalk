package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.*;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.model.order.Order;
import com.javaprojects.moonwalk.model.order.PaymentMethodType;
import com.javaprojects.moonwalk.model.rocket.*;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import com.javaprojects.moonwalk.repository.OrderRepository;
import com.javaprojects.moonwalk.service.EmailService;
import com.javaprojects.moonwalk.service.OrderService;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceUnitTest {

    private OrderRepository orderRepository;
    private RocketTripBookingService rocketTripBookingService;
    private RoomBookingService roomBookingService;
    private EmailService emailService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        rocketTripBookingService = mock(RocketTripBookingService.class);
        roomBookingService = mock(RoomBookingService.class);
        emailService = mock(EmailService.class);

        orderService = new OrderService(orderRepository, rocketTripBookingService, roomBookingService, emailService);
    }

    @Test
    void testOrder_createsOrderCorrectly() {
        String userEmail = "test@example.com";

        Room room = new Room();
        room.setPrice(100.0);
        room.setRoomNumber(101);
        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setRooms(List.of(room));
        roomBooking.setCheckInDate(LocalDateTime.now());
        roomBooking.setCheckOutDate(LocalDateTime.now().plusDays(2));
        when(roomBookingService.bookRooms(anyString(), any(RoomBookingRequest.class)))
                .thenReturn(roomBooking);

        Rocket rocket = new Rocket();
        rocket.setPrice(500.0);
        rocket.setSeatsTotal(10);
        rocket.setLaunchDay(WeekDay.MONDAY);
        rocket.setLaunchTime("10:00");

        RocketTrip rocketTrip = new RocketTrip();
        rocketTrip.setRocket(rocket);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setRocket(rocket);

        RocketTripBooking tripBooking = new RocketTripBooking();
        User user = new User();
        user.setEmail(userEmail);
        tripBooking.setUser(user);
        tripBooking.setSeatNumbers("A1 A2 A3 A4 A5");
        tripBooking.setReturnSeatNumbers("B1 B2 B3 B4 B5");
        tripBooking.setRocketTrip(rocketTrip);
        tripBooking.setReturnTrip(returnTrip);

        RocketTripBookingRequest tripRequest = new RocketTripBookingRequest();
        tripRequest.setNumPassengers(5);

        when(rocketTripBookingService.bookTrip(anyString(), any(RocketTripBookingRequest.class)))
                .thenReturn(tripBooking);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.order(userEmail, tripRequest, new RoomBookingRequest());

        BigDecimal expectedRoomPrice = BigDecimal.valueOf(100.0 * 2);
        BigDecimal expectedRocketPrice = BigDecimal.valueOf((500.0 + 500.0) * 5);
        BigDecimal expectedTotalPrice = expectedRoomPrice.add(expectedRocketPrice);

        assertEquals(expectedTotalPrice, result.getTotalPrice());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals(PaymentMethodType.CARD, result.getPaymentMethodType());

        verify(roomBookingService).bookRooms(eq(userEmail), any(RoomBookingRequest.class));
        verify(rocketTripBookingService).bookTrip(eq(userEmail), any(RocketTripBookingRequest.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testConfirmPayment_sendsEmailAndUpdatesStatus() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setOrderStatus(OrderStatus.PENDING);

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");

        Room room = new Room();
        room.setRoomNumber(101);
        room.setPrice(100.0);
        room.setHotel(hotel);

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setRooms(List.of(room));
        order.setRoomBooking(roomBooking);

        order.setRocketTripBooking(createRocketTripBooking());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.confirmPayment(1L);

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());

        verify(emailService).sendOrderDetailsEmail(
                eq("user@example.com"),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    private static RocketTripBooking createRocketTripBooking() {
        Rocket rocket = new Rocket();
        rocket.setPrice(500.0);

        RocketTrip rocketTrip = new RocketTrip();
        rocketTrip.setRocket(rocket);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setRocket(rocket);

        RocketTripBooking tripBooking = new RocketTripBooking();
        User user = new User();
        user.setEmail("user@example.com");
        tripBooking.setUser(user);
        tripBooking.setSeatNumbers("A1 A2 A3 A4 A5");
        tripBooking.setReturnSeatNumbers("B1 B2 B3 B4 B5");
        tripBooking.setRocketTrip(rocketTrip);
        tripBooking.setReturnTrip(returnTrip);
        return tripBooking;
    }

    @Test
    void testDeleteAll_callsRepository() {
        orderService.deleteAll();
        verify(orderRepository).deleteAll();
    }
}
