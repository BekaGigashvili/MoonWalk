package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.User;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTrip;
import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import com.javaprojects.moonwalk.model.rocket.RocketTripBookingRequest;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.repository.RocketTripBookingRepository;
import com.javaprojects.moonwalk.security.exceptions.UserNotFoundException;
import com.javaprojects.moonwalk.service.UserService;
import com.javaprojects.moonwalk.service.rocket.RocketTripBookingService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RocketTripBookingServiceUnitTest {

    private RocketTripBookingRepository rocketTripBookingRepository;
    private RocketTripService rocketTripService;
    private RocketTripReturnService rocketTripReturnService;
    private UserService userService;
    private RocketTripBookingService bookingService;

    @BeforeEach
    void setUp() {
        rocketTripBookingRepository = mock(RocketTripBookingRepository.class);
        rocketTripService = mock(RocketTripService.class);
        rocketTripReturnService = mock(RocketTripReturnService.class);
        userService = mock(UserService.class);

        bookingService = new RocketTripBookingService(
                rocketTripBookingRepository,
                rocketTripService,
                rocketTripReturnService,
                userService
        );
    }

    @Test
    void bookTrip_successfulBooking() {
        User user = new User();
        user.setId(1L);

        Rocket rocket = new Rocket();
        rocket.setSeatsTotal(10);

        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);
        trip.setSeatsTaken(2);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setId(2L);
        returnTrip.setRocket(rocket);
        returnTrip.setSeatsTaken(3);

        RocketTripBookingRequest request = new RocketTripBookingRequest();
        request.setTripId(1L);
        request.setReturnTripId(2L);
        request.setNumPassengers(2);

        when(rocketTripService.findByIdForUpdate(1L)).thenReturn(trip);
        when(rocketTripReturnService.findByIdForUpdate(2L)).thenReturn(returnTrip);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        RocketTripBooking booking = bookingService.bookTrip("test@example.com", request);

        assertNotNull(booking);
        assertEquals(user, booking.getUser());
        assertEquals(trip, booking.getRocketTrip());
        assertEquals(returnTrip, booking.getReturnTrip());
        assertTrue(booking.getSeatNumbers().contains("A3"));
        assertTrue(booking.getReturnSeatNumbers().contains("A4"));

        verify(rocketTripService).save(trip);
        verify(rocketTripReturnService).save(returnTrip);
        verify(rocketTripBookingRepository).save(booking);
    }

    @Test
    void bookTrip_throwsIfNotEnoughSeats() {
        Rocket rocket = new Rocket();
        rocket.setSeatsTotal(2);

        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);
        trip.setSeatsTaken(2);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setId(2L);
        returnTrip.setRocket(rocket);
        returnTrip.setSeatsTaken(1);

        RocketTripBookingRequest request = new RocketTripBookingRequest();
        request.setTripId(1L);
        request.setReturnTripId(2L);
        request.setNumPassengers(1);

        when(rocketTripService.findByIdForUpdate(1L)).thenReturn(trip);
        when(rocketTripReturnService.findByIdForUpdate(2L)).thenReturn(returnTrip);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.bookTrip("test@example.com", request));
        assertEquals("Not enough seats available", ex.getMessage());
    }

    @Test
    void bookTrip_throwsIfNotEnoughReturnSeats() {
        Rocket rocket = new Rocket();
        rocket.setSeatsTotal(5);

        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);
        trip.setSeatsTaken(1);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setId(2L);
        returnTrip.setRocket(rocket);
        returnTrip.setSeatsTaken(5);

        RocketTripBookingRequest request = new RocketTripBookingRequest();
        request.setTripId(1L);
        request.setReturnTripId(2L);
        request.setNumPassengers(2);

        when(rocketTripService.findByIdForUpdate(1L)).thenReturn(trip);
        when(rocketTripReturnService.findByIdForUpdate(2L)).thenReturn(returnTrip);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.bookTrip("test@example.com", request));
        assertEquals("Not enough seats available for return", ex.getMessage());
    }

    @Test
    void bookTrip_throwsIfUserNotFound() {
        Rocket rocket = new Rocket();
        rocket.setSeatsTotal(5);

        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);
        trip.setSeatsTaken(0);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setId(2L);
        returnTrip.setRocket(rocket);
        returnTrip.setSeatsTaken(0);

        RocketTripBookingRequest request = new RocketTripBookingRequest();
        request.setTripId(1L);
        request.setReturnTripId(2L);
        request.setNumPassengers(1);

        when(rocketTripService.findByIdForUpdate(1L)).thenReturn(trip);
        when(rocketTripReturnService.findByIdForUpdate(2L)).thenReturn(returnTrip);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookingService.bookTrip("test@example.com", request));
    }

    @Test
    void save_delegatesToRepository() {
        RocketTripBooking booking = new RocketTripBooking();
        bookingService.save(booking);
        verify(rocketTripBookingRepository).save(booking);
    }

    @Test
    void delete_delegatesToRepository() {
        RocketTripBooking booking = new RocketTripBooking();
        bookingService.delete(booking);
        verify(rocketTripBookingRepository).delete(booking);
    }

    @Test
    void deleteAll_delegatesToRepository() {
        bookingService.deleteAll();
        verify(rocketTripBookingRepository).deleteAll();
    }
}
