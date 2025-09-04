package com.javaprojects.moonwalk.service.room;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import com.javaprojects.moonwalk.model.room.RoomRequest;
import com.javaprojects.moonwalk.repository.RoomBookingRepository;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import com.javaprojects.moonwalk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomBookingService {
    private final RoomBookingRepository roomBookingRepository;
    private final RoomService roomService;
    private final UserService userService;
    private final RoomAvailabilityService availabilityService;
    private final RocketTripService rocketTripService;
    private final RocketTripReturnService rocketTripReturnService;

    @Transactional
    public RoomBooking bookRooms(String userEmail, RoomBookingRequest request) {

        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Room> bookedRooms = new ArrayList<>();

        LocalDateTime checkInDate = rocketTripService
                .findById(request.getTripId())
                .getLaunchExactTime().plusHours(1);

        LocalDateTime checkOutDate = rocketTripReturnService
                .findById(request.getReturnTripId())
                .getReturnExactTime().minusHours(1);

        for (RoomRequest roomRequest : request.getRooms()) {
            int capacity = roomRequest.getCapacity();
            int quantityNeeded = roomRequest.getQuantity();

            List<Room> availableRooms = roomService
                    .findRoomsByHotelIdAndCapacityForUpdate(
                            request.getHotelId(),
                            capacity
                    );

            availableRooms = availabilityService
                    .filterAvailableRooms(
                            availableRooms,
                            checkInDate,
                            checkOutDate
                    );

            if (availableRooms.size() < quantityNeeded) {
                throw new IllegalStateException("Not enough rooms of capacity " + capacity + " available for requested dates");
            }

            bookedRooms.addAll(availableRooms.subList(0, quantityNeeded));
        }

        for(Room room : bookedRooms) {
            roomService.setStatus(room.getHotel().getId(), room.getRoomNumber(), RoomStatus.RESERVED);
        }

        RoomBooking booking = RoomBooking.builder()
                .user(user)
                .rooms(bookedRooms)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .build();

        roomBookingRepository.save(booking);
        return booking;
    }

    public void delete(RoomBooking booking) {
        roomBookingRepository.delete(booking);
    }

    public void deleteAll(){
        roomBookingRepository.deleteAll();
    }

    public RoomBooking save(RoomBooking roomBooking) {
        return roomBookingRepository.save(roomBooking);
    }
}
