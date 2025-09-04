package com.javaprojects.moonwalk.service;

import com.javaprojects.moonwalk.model.OrderStatus;
import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.order.Order;
import com.javaprojects.moonwalk.model.rocket.RocketTrip;
import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTaskService {
    private final OrderService orderService;
    private final RocketTripBookingService rocketTripBookingService;
    private final RoomBookingService roomBookingService;
    private final RocketTripService rocketTripService;
    private final RocketTripReturnService rocketTripReturnService;

    @Scheduled(fixedRate = 1000 * 60 * 15)
    @Transactional
    public void rollBackUnconfirmedOrders() {
        List<Order> orders = orderService
                .findPendingOlderThan(OrderStatus.PENDING, LocalDateTime.now().minusMinutes(15));

        for (Order order : orders) {
            RocketTripBooking rocketTripBooking = order.getRocketTripBooking();
            RoomBooking roomBooking = order.getRoomBooking();
            List<Room> rooms = roomBooking.getRooms();

            for(Room room : rooms){
                room.setStatus(RoomStatus.AVAILABLE);
            }

            RocketTrip rocketTrip = rocketTripBooking.getRocketTrip();
            RocketTripReturn rocketTripReturn = rocketTripBooking.getReturnTrip();

            rocketTrip.setSeatsTaken(rocketTrip.getSeatsTaken() - order.getNumPassengers());
            rocketTripService.save(rocketTrip);
            rocketTripReturn.setSeatsTaken(rocketTripReturn.getSeatsTaken() - order.getNumPassengers());
            rocketTripReturnService.save(rocketTripReturn);

            order.setRocketTripBooking(null);
            order.setRoomBooking(null);

            rocketTripBookingService.delete(rocketTripBooking);

            roomBookingService.delete(roomBooking);

            order.setOrderStatus(OrderStatus.CANCELLED);
        }
    }
}
