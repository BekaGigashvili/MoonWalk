package com.javaprojects.moonwalk.service.room;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.room.AddRoomRequest;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomResponse;
import com.javaprojects.moonwalk.repository.RoomRepository;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomAvailabilityService availabilityService;
    private final RocketTripService tripService;
    private final RocketTripReturnService tripReturnService;
    private final HotelService hotelService;

    public Map<Integer, Integer> getAvailableCapacities(Long hotelId, Long launchTripId, Long returnTripId) {
        List<Room> rooms = roomRepository.findRoomsByHotelId(hotelId);
        LocalDateTime checkIn = tripService.findById(launchTripId).getLaunchExactTime().plusHours(1);
        LocalDateTime checkOut = tripReturnService.findById(returnTripId).getReturnExactTime().minusHours(1);
        List<Room> availableRooms = availabilityService.filterAvailableRooms(rooms, checkIn, checkOut);

        Map<Integer, Integer> capacities = new HashMap<>();
        for (Room room : availableRooms) {
            capacities.put(room.getCapacity(),
                    capacities.getOrDefault(room.getCapacity(), 0) + 1);
        }
        return capacities;
    }

    public List<Room> findRoomsByHotelIdAndCapacityForUpdate(Long hotelId, Integer capacity) {
        return roomRepository.findRoomsByHotelIdAndCapacityForUpdate(hotelId, capacity);
    }

    @Transactional
    public void addRoom(Long hotelId, AddRoomRequest request) {
        List<Room> rooms = roomRepository.findRoomsByHotelId(hotelId);
        for (Room room : rooms) {
            if(room.getRoomNumber() == request.getRoomNumber()) {
                throw new IllegalStateException("Room number already exists");
            }
        }
        Room room = Room
                .builder()
                .roomNumber(request.getRoomNumber())
                .capacity(request.getCapacity())
                .price(request.getPrice())
                .status(RoomStatus.AVAILABLE)
                .hotel(hotelService.findById(hotelId))
                .build();
        roomRepository.save(room);
    }

    @Transactional
    public void setStatus(Long hotelId, int roomNumber, RoomStatus status) {
        Room room = roomRepository.findRoomByHotelIdAndRoomNumber(hotelId, roomNumber);
        room.setStatus(status);
        roomRepository.save(room);
    }

    public List<RoomResponse> findRoomsByHotelIdAndStatus(Long hotelId, RoomStatus status) {
        List<Room> rooms = roomRepository.findRoomsByHotelIdAndStatus(hotelId, status);
        List<RoomResponse> response = new ArrayList<>();
        for (Room room : rooms) {
            RoomResponse roomResponse = RoomResponse
                    .builder()
                    .roomNumber(room.getRoomNumber())
                    .price(room.getPrice())
                    .status(room.getStatus())
                    .capacity(room.getCapacity())
                    .build();
            response.add(roomResponse);
        }
        return response;
    }

    public List<RoomResponse> findAllRoomsByHotelId(Long hotelId) {
        List<Room> rooms = roomRepository.findRoomsByHotelId(hotelId);
        List<RoomResponse> response = new ArrayList<>();
        for (Room room : rooms) {
            RoomResponse roomResponse = RoomResponse
                    .builder()
                    .roomNumber(room.getRoomNumber())
                    .capacity(room.getCapacity())
                    .price(room.getPrice())
                    .status(room.getStatus())
                    .build();
            response.add(roomResponse);
        }
        return response;
    }

    public Room save(Room room){
        return roomRepository.save(room);
    }

    public void deleteAll() {
        roomRepository.deleteAll();
    }

    public void saveAll(List<Room> rooms) {
        roomRepository.saveAll(rooms);
    }
}
