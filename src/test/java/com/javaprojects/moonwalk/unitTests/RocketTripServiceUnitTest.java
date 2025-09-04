package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.rocket.*;
import com.javaprojects.moonwalk.repository.RocketTripRepository;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RocketTripServiceUnitTest {

    private RocketTripRepository rocketTripRepository;
    private RocketService rocketService;
    private RocketTripReturnService rocketTripReturnService;
    private RocketTripService rocketTripService;

    @BeforeEach
    void setUp() {
        rocketTripRepository = mock(RocketTripRepository.class);
        rocketService = mock(RocketService.class);
        rocketTripReturnService = mock(RocketTripReturnService.class);

        rocketTripService = new RocketTripService(
                rocketTripRepository,
                rocketService,
                rocketTripReturnService
        );
    }

    @Test
    void findById_returnsRocketTrip() {
        Rocket rocket = new Rocket();
        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);

        when(rocketTripRepository.findById(1L)).thenReturn(java.util.Optional.of(trip));

        RocketTrip result = rocketTripService.findById(1L);

        assertEquals(trip, result);
    }

    @Test
    void findById_throwsIfNotFound() {
        when(rocketTripRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripService.findById(1L));
        assertEquals("Could not find rocket trip with id: 1", ex.getMessage());
    }

    @Test
    void deleteAll_delegatesToRepository() {
        rocketTripService.deleteAll();
        verify(rocketTripRepository).deleteAll();
    }

    @Test
    void create_savesTripAndReturnTripSuccessfully() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);
        rocket.setSeatsTotal(10);

        RocketTripCreateRequest request = new RocketTripCreateRequest();
        request.setRocketId(1L);
        request.setLaunchExactTime(LocalDateTime.now().plusDays(10));

        when(rocketService.findById(1L)).thenReturn(rocket);

        rocketTripService.create(request);

        verify(rocketTripReturnService).save(any(RocketTripReturn.class));
        verify(rocketTripRepository).save(any(RocketTrip.class));
    }

    @Test
    void create_throwsIfRocketNotFound() {
        RocketTripCreateRequest request = new RocketTripCreateRequest();
        request.setRocketId(1L);
        request.setLaunchExactTime(LocalDateTime.now().plusDays(10));

        when(rocketService.findById(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripService.create(request));
        assertEquals("Rocket not found", ex.getMessage());
    }

    @Test
    void create_throwsIfLaunchTimeNotProvided() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);

        RocketTripCreateRequest request = new RocketTripCreateRequest();
        request.setRocketId(1L);
        request.setLaunchExactTime(null);

        when(rocketService.findById(1L)).thenReturn(rocket);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripService.create(request));
        assertEquals("Launch exact time not found", ex.getMessage());
    }

    @Test
    void create_throwsIfLaunchTimeTooEarly() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);

        RocketTripCreateRequest request = new RocketTripCreateRequest();
        request.setRocketId(1L);
        request.setLaunchExactTime(LocalDateTime.now().plusDays(3));

        when(rocketService.findById(1L)).thenReturn(rocket);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripService.create(request));
        assertEquals("Launch exact time too early", ex.getMessage());
    }

    @Test
    void findAll_returnsResponses() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);

        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);
        trip.setLaunchExactTime(LocalDateTime.now().plusDays(10));
        trip.setSeatsTaken(5);

        when(rocketTripRepository.findAll()).thenReturn(List.of(trip));

        List<RocketTripResponse> responses = rocketTripService.findAll();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(1L, responses.get(0).getRocketId());
    }

    @Test
    void findTripBySeatsAndTime_returnsCorrectMap() {
        Rocket rocket = new Rocket();
        rocket.setSeatsTotal(10);

        RocketTrip trip = new RocketTrip();
        trip.setId(1L);
        trip.setRocket(rocket);
        trip.setLaunchExactTime(LocalDateTime.now().plusDays(2));
        trip.setSeatsTaken(2);

        when(rocketTripRepository.findAll()).thenReturn(List.of(trip));

        Map<Long, LocalDateTime> result = rocketTripService.findTripBySeatsAndTime(2);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
    }

    @Test
    void findTripBySeatsAndTime_throwsIfNoTripsFound() {
        when(rocketTripRepository.findAll()).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripService.findTripBySeatsAndTime(2));
        assertEquals("No trips found", ex.getMessage());
    }

    @Test
    void findByIdForUpdate_delegatesToRepository() {
        RocketTrip trip = new RocketTrip();
        when(rocketTripRepository.findByIdForUpdate(1L)).thenReturn(trip);

        RocketTrip result = rocketTripService.findByIdForUpdate(1L);

        assertEquals(trip, result);
    }

    @Test
    void save_delegatesToRepository() {
        RocketTrip trip = new RocketTrip();
        rocketTripService.save(trip);
        verify(rocketTripRepository).save(trip);
    }
}
