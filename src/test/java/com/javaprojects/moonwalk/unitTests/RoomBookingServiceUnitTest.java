package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.room.Room;
import com.javaprojects.moonwalk.model.room.RoomBooking;
import com.javaprojects.moonwalk.model.room.RoomBookingRequest;
import com.javaprojects.moonwalk.model.room.RoomRequest;
import com.javaprojects.moonwalk.repository.RoomBookingRepository;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.room.RoomAvailabilityService;
import com.javaprojects.moonwalk.service.room.RoomBookingService;
import com.javaprojects.moonwalk.service.room.RoomService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomBookingServiceUnitTest {

    private RoomBookingRepository roomBookingRepository;
    private RoomService roomService;
    private UserService userService;
    private RoomAvailabilityService availabilityService;
    private RocketTripService rocketTripService;
    private RocketTripReturnService rocketTripReturnService;
    private RoomBookingService bookingService;

    @BeforeEach
    void setUp() {
        roomBookingRepository = mock(RoomBookingRepository.class);
        roomService = mock(RoomService.class);
        userService = mock(UserService.class);
        availabilityService = mock(RoomAvailabilityService.class);
        rocketTripService = mock(RocketTripService.class);
        rocketTripReturnService = mock(RocketTripReturnService.class);

        bookingService = new RoomBookingService(
                roomBookingRepository,
                roomService,
                userService,
                availabilityService,
                rocketTripService,
                rocketTripReturnService
        );
    }

    @Test
    void bookRooms_success() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        com.javaprojects.moonwalk.model.hotel.Hotel hotel = new com.javaprojects.moonwalk.model.hotel.Hotel();
        hotel.setId(1L);

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomNumber(101);
        room.setCapacity(2);

        when(roomService.findRoomsByHotelIdAndCapacityForUpdate(1L, 2)).thenReturn(List.of(room));
        when(availabilityService.filterAvailableRooms(anyList(), any(), any())).thenReturn(List.of(room));
        when(rocketTripService.findById(1L)).thenReturn(new com.javaprojects.moonwalk.model.rocket.RocketTrip(null, LocalDateTime.now(), null, 0, null));
        when(rocketTripReturnService.findById(1L)).thenReturn(new com.javaprojects.moonwalk.model.rocket.RocketTripReturn(null, LocalDateTime.now().plusHours(3), null, 0, null));

        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setCapacity(2);
        roomRequest.setQuantity(1);

        RoomBookingRequest bookingRequest = new RoomBookingRequest();
        bookingRequest.setHotelId(1L);
        bookingRequest.setTripId(1L);
        bookingRequest.setReturnTripId(1L);
        bookingRequest.setRooms(List.of(roomRequest));

        RoomBooking booking = bookingService.bookRooms("test@example.com", bookingRequest);

        assertNotNull(booking);
        assertEquals(user, booking.getUser());
        assertEquals(1, booking.getRooms().size());
        verify(roomService).setStatus(eq(1L), eq(101), eq(RoomStatus.RESERVED));
        verify(roomBookingRepository).save(booking);
    }


    @Test
    void bookRooms_throwsIfNotEnoughRooms() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        com.javaprojects.moonwalk.model.hotel.Hotel hotel = new com.javaprojects.moonwalk.model.hotel.Hotel();
        hotel.setId(1L);

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomNumber(101);
        room.setCapacity(2);

        when(roomService.findRoomsByHotelIdAndCapacityForUpdate(1L, 2)).thenReturn(List.of(room));
        when(availabilityService.filterAvailableRooms(anyList(), any(), any())).thenReturn(List.of(room));

        when(rocketTripService.findById(1L)).thenReturn(new com.javaprojects.moonwalk.model.rocket.RocketTrip(null, LocalDateTime.now(), null, 0, null));
        when(rocketTripReturnService.findById(1L)).thenReturn(new com.javaprojects.moonwalk.model.rocket.RocketTripReturn(null, LocalDateTime.now().plusHours(3), null, 0, null));

        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setCapacity(2);
        roomRequest.setQuantity(2);

        RoomBookingRequest request = new RoomBookingRequest();
        request.setHotelId(1L);
        request.setTripId(1L);
        request.setReturnTripId(1L);
        request.setRooms(List.of(roomRequest));

        assertThrows(IllegalStateException.class, () -> bookingService.bookRooms("test@example.com", request));
    }


    @Test
    void bookRooms_throwsIfUserNotFound() {
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        RoomBookingRequest bookingRequest = new RoomBookingRequest();
        assertThrows(IllegalStateException.class, () -> bookingService.bookRooms("missing@example.com", bookingRequest));
    }

    @Test
    void delete_callsRepository() {
        RoomBooking booking = new RoomBooking();
        bookingService.delete(booking);
        verify(roomBookingRepository).delete(booking);
    }

    @Test
    void deleteAll_callsRepository() {
        bookingService.deleteAll();
        verify(roomBookingRepository).deleteAll();
    }
}
