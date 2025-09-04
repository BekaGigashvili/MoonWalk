package com.javaprojects.moonwalk.model.order;

import com.javaprojects.moonwalk.model.OrderStatus;
import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "rocket_Trip_Booking_id")
    private RocketTripBooking rocketTripBooking;
    @ManyToOne
    @JoinColumn(name = "room_booking_id")
    private RoomBooking roomBooking;
    private LocalDateTime orderDate;
    @Column(nullable = false)
    private BigDecimal totalPrice;
    private int numPassengers;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
