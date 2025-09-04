package com.javaprojects.moonwalk.service.room;

import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.repository.RoomBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
    private final RoomBookingRepository roomBookingRepository;

    public boolean isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return !roomBookingRepository.existsByRoomAndDateOverlap(roomId, checkIn, checkOut);
    }

    public List<Room> filterAvailableRooms(List<Room> rooms, LocalDateTime checkIn, LocalDateTime checkOut) {
        return rooms.stream()
                .filter(r -> isRoomAvailable(r.getId(), checkIn, checkOut))
                .toList();
    }
}

