package com.javaprojects.moonwalk.controller;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.model.room.AddRoomRequest;
import com.javaprojects.moonwalk.model.room.RoomResponse;
import com.javaprojects.moonwalk.security.exceptions.UserNotFoundException;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.room.RoomService;
import com.javaprojects.moonwalk.model.room.SetStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {
    private final RoomService roomService;
    private final HotelService hotelService;
    private final UserService userService;

    @GetMapping("/capacities/{hotelId}/{tripId}/{returnTripId}")
    public Map<Integer, Integer> getCapacities(
            @PathVariable Long hotelId,
            @PathVariable Long tripId,
            @PathVariable Long returnTripId
    ) {
        return roomService.getAvailableCapacities(hotelId, tripId, returnTripId);
    }

    @GetMapping
    public List<RoomResponse> getAllByHotel(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Hotel hotel = hotelService.findByEmail(user.getEmail());
        return roomService.findAllRoomsByHotelId(hotel.getId());
    }

    @GetMapping("/available")
    public List<RoomResponse> getAvailableRooms(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        Hotel hotel = hotelService.findByEmail(user.getEmail());
        return roomService.findRoomsByHotelIdAndStatus(hotel.getId(), RoomStatus.AVAILABLE);
    }

    @GetMapping("/reserved")
    public List<RoomResponse> getReservedRooms(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        Hotel hotel = hotelService.findByEmail(user.getEmail());
        return roomService.findRoomsByHotelIdAndStatus(hotel.getId(), RoomStatus.RESERVED);
    }

    @GetMapping("/occupied")
    public List<RoomResponse> getOccupiedRooms(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        Hotel hotel = hotelService.findByEmail(user.getEmail());
        return roomService.findRoomsByHotelIdAndStatus(hotel.getId(), RoomStatus.OCCUPIED);
    }

    @PostMapping("/status")
    public void setStatus(Authentication auth,
                          @RequestBody SetStatusRequest setStatusRequest) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        Hotel hotel = hotelService.findByEmail(user.getEmail());
        roomService.setStatus(hotel.getId(), setStatusRequest.getRoomNumber(), setStatusRequest.getStatus());
    }

    @PostMapping("/room")
    public void addRoom(Authentication auth, @RequestBody AddRoomRequest request) {
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ;
        Hotel hotel = hotelService.findByEmail(user.getEmail());
        roomService.addRoom(hotel.getId(), request);
    }
}
