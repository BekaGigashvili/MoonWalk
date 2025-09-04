package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.*;
import com.javaprojects.moonwalk.model.order.Order;
import com.javaprojects.moonwalk.model.rocket.*;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.service.*;
import com.javaprojects.moonwalk.service.rocket.*;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

class ScheduledTaskServiceUnitTest {

    private ScheduledTaskService scheduledTaskService;
    private OrderService orderService;
    private RocketTripBookingService rocketTripBookingService;
    private RoomBookingService roomBookingService;
    private RocketTripService rocketTripService;
    private RocketTripReturnService rocketTripReturnService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        rocketTripBookingService = mock(RocketTripBookingService.class);
        roomBookingService = mock(RoomBookingService.class);
        rocketTripService = mock(RocketTripService.class);
        rocketTripReturnService = mock(RocketTripReturnService.class);

        scheduledTaskService = new ScheduledTaskService(
                orderService,
                rocketTripBookingService,
                roomBookingService,
                rocketTripService,
                rocketTripReturnService
        );
    }

    @Test
    void rollBackUnconfirmedOrders_rollsBackPendingOrders() {
        RocketTrip rocketTrip = new RocketTrip();
        rocketTrip.setSeatsTaken(5);

        RocketTripReturn rocketTripReturn = new RocketTripReturn();
        rocketTripReturn.setSeatsTaken(3);

        RocketTripBooking rocketTripBooking = new RocketTripBooking();
        rocketTripBooking.setRocketTrip(rocketTrip);
        rocketTripBooking.setReturnTrip(rocketTripReturn);

        Room room = new Room();
        room.setStatus(RoomStatus.RESERVED);

        RoomBooking roomBooking = new RoomBooking();
        roomBooking.setRooms(Collections.singletonList(room));

        Order order = new Order();
        order.setNumPassengers(2);
        order.setRocketTripBooking(rocketTripBooking);
        order.setRoomBooking(roomBooking);
        order.setOrderStatus(OrderStatus.PENDING);

        when(orderService.findPendingOlderThan(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(order));

        scheduledTaskService.rollBackUnconfirmedOrders();

        assert(room.getStatus() == RoomStatus.AVAILABLE);

        assert(rocketTrip.getSeatsTaken() == 3);
        assert(rocketTripReturn.getSeatsTaken() == 1);

        verify(rocketTripService).save(rocketTrip);
        verify(rocketTripReturnService).save(rocketTripReturn);
        verify(rocketTripBookingService).delete(rocketTripBooking);
        verify(roomBookingService).delete(roomBooking);

        assert(order.getOrderStatus() == OrderStatus.CANCELLED);
    }

    @Test
    void rollBackUnconfirmedOrders_noPendingOrders_nothingHappens() {
        when(orderService.findPendingOlderThan(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduledTaskService.rollBackUnconfirmedOrders();

        verifyNoInteractions(rocketTripBookingService, roomBookingService, rocketTripService, rocketTripReturnService);
    }
}
