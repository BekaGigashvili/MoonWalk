package com.javaprojects.moonwalk.service;

import com.javaprojects.moonwalk.model.OrderStatus;
import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.order.Order;
import com.javaprojects.moonwalk.model.order.PaymentMethodType;
import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import com.javaprojects.moonwalk.model.rocket.RocketTripBookingRequest;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import com.javaprojects.moonwalk.repository.OrderRepository;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RocketTripBookingService rocketTripBookingService;
    private final RoomBookingService roomBookingService;
    private final EmailService emailService;

    @Transactional
    public void confirmPayment(Long orderId) throws MessagingException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        RoomBooking roomBooking = order.getRoomBooking();
        RocketTripBooking tripBooking = order.getRocketTripBooking();

        String hotelName = roomBooking.getRooms().get(0).getHotel().getName();

        StringBuilder roomNumbers = new StringBuilder();
        for(Room room : roomBooking.getRooms()){
            roomNumbers.append(room.getRoomNumber()).append(", ");
        }

        String userEmail = tripBooking.getUser().getEmail();

        emailService.sendOrderDetailsEmail(
                userEmail,
                hotelName,
                roomNumbers.toString().trim(),
                tripBooking.getSeatNumbers(),
                tripBooking.getReturnSeatNumbers()
        );
    }

    @Transactional
    public Order order(
            String userEmail,
            RocketTripBookingRequest tripBookingRequest,
            RoomBookingRequest roomBookingRequest
            ) {
        RoomBooking roomBooking = roomBookingService.bookRooms(
                userEmail,
                roomBookingRequest
        );

        RocketTripBooking tripBooking = rocketTripBookingService.bookTrip(
                userEmail,
                tripBookingRequest
        );

        List<Room> rooms = roomBooking.getRooms();
        for(Room room : rooms){
            room.setStatus(RoomStatus.RESERVED);
        }

        long days = ChronoUnit.DAYS
                .between(roomBooking.getCheckInDate().toLocalDate(),
                        roomBooking.getCheckOutDate().toLocalDate());

        BigDecimal roomsTotal = rooms.stream()
                .map(room -> BigDecimal.valueOf(room.getPrice()).multiply(BigDecimal.valueOf(days)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal rocketToMoon = BigDecimal.valueOf(tripBooking.getRocketTrip().getRocket().getPrice())
                .multiply(BigDecimal.valueOf(tripBookingRequest.getNumPassengers()));

        BigDecimal rocketToEarth = BigDecimal.valueOf(tripBooking.getReturnTrip().getRocket().getPrice())
                .multiply(BigDecimal.valueOf(tripBookingRequest.getNumPassengers()));

        BigDecimal totalPrice = roomsTotal.add(rocketToMoon).add(rocketToEarth);

        Order order = Order
                .builder()
                .rocketTripBooking(tripBooking)
                .roomBooking(roomBooking)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .paymentMethodType(PaymentMethodType.CARD)
                .orderDate(LocalDateTime.now())
                .numPassengers(tripBookingRequest.getNumPassengers())
                .build();

        return orderRepository.save(order);
    }

    public List<Order> findPendingOlderThan(OrderStatus status, LocalDateTime localDateTime) {
        return orderRepository.findPendingOlderThan(status, localDateTime);
    }

    public void deleteAll(){
        orderRepository.deleteAll();
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
