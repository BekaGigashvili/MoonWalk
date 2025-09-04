package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.model.room.AddRoomRequest;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomResponse;
import com.javaprojects.moonwalk.repository.RoomRepository;
import com.javaprojects.moonwalk.service.HotelService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import com.javaprojects.moonwalk.service.room.RoomAvailabilityService;
import com.javaprojects.moonwalk.service.room.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceUnitTest {

    private RoomRepository roomRepository;
    private RoomAvailabilityService availabilityService;
    private RocketTripService tripService;
    private RocketTripReturnService tripReturnService;
    private HotelService hotelService;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        availabilityService = mock(RoomAvailabilityService.class);
        tripService = mock(RocketTripService.class);
        tripReturnService = mock(RocketTripReturnService.class);
        hotelService = mock(HotelService.class);

        roomService = new RoomService(
                roomRepository,
                availabilityService,
                tripService,
                tripReturnService,
                hotelService
        );
    }

    @Test
    void getAvailableCapacities_returnsCorrectMap() {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setCapacity(2);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setCapacity(3);

        when(roomRepository.findRoomsByHotelId(1L)).thenReturn(List.of(room1, room2));
        when(tripService.findById(1L)).thenReturn(new com.javaprojects.moonwalk.model.rocket.RocketTrip(null, LocalDateTime.now(), null, 0, null));
        when(tripReturnService.findById(1L)).thenReturn(new com.javaprojects.moonwalk.model.rocket.RocketTripReturn(null, LocalDateTime.now().plusHours(3), null, 0, null));
        when(availabilityService.filterAvailableRooms(anyList(), any(), any())).thenReturn(List.of(room1, room2));

        Map<Integer, Integer> capacities = roomService.getAvailableCapacities(1L, 1L, 1L);

        assertEquals(2, capacities.size());
        assertEquals(1, capacities.get(2));
        assertEquals(1, capacities.get(3));
    }

    @Test
    void addRoom_savesRoomCorrectly() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        AddRoomRequest request = new AddRoomRequest();
        request.setRoomNumber(101);
        request.setCapacity(2);
        request.setPrice(100.0);

        when(roomRepository.findRoomsByHotelId(1L)).thenReturn(List.of());
        when(hotelService.findById(1L)).thenReturn(hotel);

        roomService.addRoom(1L, request);

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void addRoom_throwsExceptionIfRoomNumberExists() {
        Room existingRoom = new Room();
        existingRoom.setRoomNumber(101);
        when(roomRepository.findRoomsByHotelId(1L)).thenReturn(List.of(existingRoom));

        AddRoomRequest request = new AddRoomRequest();
        request.setRoomNumber(101);

        assertThrows(IllegalStateException.class, () -> roomService.addRoom(1L, request));
    }

    @Test
    void setStatus_updatesRoomStatus() {
        Room room = new Room();
        room.setRoomNumber(101);

        when(roomRepository.findRoomByHotelIdAndRoomNumber(1L, 101)).thenReturn(room);

        roomService.setStatus(1L, 101, RoomStatus.OCCUPIED);

        assertEquals(RoomStatus.OCCUPIED, room.getStatus());
        verify(roomRepository).save(room);
    }

    @Test
    void findRoomsByHotelIdAndStatus_returnsRoomResponses() {
        Room room = new Room();
        room.setRoomNumber(101);
        room.setCapacity(2);
        room.setPrice(100.0);
        room.setStatus(RoomStatus.AVAILABLE);

        when(roomRepository.findRoomsByHotelIdAndStatus(1L, RoomStatus.AVAILABLE)).thenReturn(List.of(room));

        List<RoomResponse> responses = roomService.findRoomsByHotelIdAndStatus(1L, RoomStatus.AVAILABLE);

        assertEquals(1, responses.size());
        assertEquals(101, responses.get(0).getRoomNumber());
    }

    @Test
    void findAllRoomsByHotelId_returnsRoomResponses() {
        Room room = new Room();
        room.setRoomNumber(101);
        room.setCapacity(2);
        room.setPrice(100.0);
        room.setStatus(RoomStatus.AVAILABLE);

        when(roomRepository.findRoomsByHotelId(1L)).thenReturn(List.of(room));

        List<RoomResponse> responses = roomService.findAllRoomsByHotelId(1L);

        assertEquals(1, responses.size());
        assertEquals(101, responses.get(0).getRoomNumber());
    }

    @Test
    void save_delegatesToRepository() {
        Room room = new Room();
        roomService.save(room);
        verify(roomRepository).save(room);
    }

    @Test
    void deleteAll_delegatesToRepository() {
        roomService.deleteAll();
        verify(roomRepository).deleteAll();
    }

    @Test
    void saveAll_delegatesToRepository() {
        Room room1 = new Room();
        Room room2 = new Room();
        roomService.saveAll(List.of(room1, room2));
        verify(roomRepository).saveAll(List.of(room1, room2));
    }
}
