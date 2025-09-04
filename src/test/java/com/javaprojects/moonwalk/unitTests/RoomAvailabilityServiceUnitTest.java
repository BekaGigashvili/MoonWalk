package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.repository.RoomBookingRepository;
import com.javaprojects.moonwalk.service.room.RoomAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomAvailabilityServiceUnitTest {

    private RoomBookingRepository roomBookingRepository;
    private RoomAvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        roomBookingRepository = mock(RoomBookingRepository.class);
        availabilityService = new RoomAvailabilityService(roomBookingRepository);
    }

    @Test
    void isRoomAvailable_whenNoOverlap_returnsTrue() {
        Long roomId = 1L;
        LocalDateTime checkIn = LocalDateTime.of(2025, 9, 5, 14, 0);
        LocalDateTime checkOut = LocalDateTime.of(2025, 9, 10, 12, 0);

        when(roomBookingRepository.existsByRoomAndDateOverlap(roomId, checkIn, checkOut))
                .thenReturn(false);

        boolean available = availabilityService.isRoomAvailable(roomId, checkIn, checkOut);

        assertTrue(available);
        verify(roomBookingRepository).existsByRoomAndDateOverlap(roomId, checkIn, checkOut);
    }

    @Test
    void isRoomAvailable_whenOverlap_returnsFalse() {
        Long roomId = 1L;
        LocalDateTime checkIn = LocalDateTime.of(2025, 9, 5, 14, 0);
        LocalDateTime checkOut = LocalDateTime.of(2025, 9, 10, 12, 0);

        when(roomBookingRepository.existsByRoomAndDateOverlap(roomId, checkIn, checkOut))
                .thenReturn(true);

        boolean available = availabilityService.isRoomAvailable(roomId, checkIn, checkOut);

        assertFalse(available);
        verify(roomBookingRepository).existsByRoomAndDateOverlap(roomId, checkIn, checkOut);
    }

    @Test
    void filterAvailableRooms_returnsOnlyAvailableRooms() {
        Room room1 = new Room();
        room1.setId(1L);

        Room room2 = new Room();
        room2.setId(2L);

        List<Room> rooms = List.of(room1, room2);
        LocalDateTime checkIn = LocalDateTime.of(2025, 9, 5, 14, 0);
        LocalDateTime checkOut = LocalDateTime.of(2025, 9, 10, 12, 0);

        when(roomBookingRepository.existsByRoomAndDateOverlap(room1.getId(), checkIn, checkOut))
                .thenReturn(false);
        when(roomBookingRepository.existsByRoomAndDateOverlap(room2.getId(), checkIn, checkOut))
                .thenReturn(true);

        List<Room> availableRooms = availabilityService.filterAvailableRooms(rooms, checkIn, checkOut);

        assertEquals(1, availableRooms.size());
        assertEquals(room1, availableRooms.get(0));

        verify(roomBookingRepository).existsByRoomAndDateOverlap(room1.getId(), checkIn, checkOut);
        verify(roomBookingRepository).existsByRoomAndDateOverlap(room2.getId(), checkIn, checkOut);
    }
}
