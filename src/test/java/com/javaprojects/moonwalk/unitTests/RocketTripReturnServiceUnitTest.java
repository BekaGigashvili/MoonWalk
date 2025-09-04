package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturnResponse;
import com.javaprojects.moonwalk.repository.RocketTripReturnRepository;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RocketTripReturnServiceUnitTest {

    private RocketTripReturnRepository rocketTripReturnRepository;
    private RocketTripReturnService rocketTripReturnService;

    @BeforeEach
    void setUp() {
        rocketTripReturnRepository = mock(RocketTripReturnRepository.class);
        rocketTripReturnService = new RocketTripReturnService(rocketTripReturnRepository);
    }

    @Test
    void findById_returnsRocketTripReturn() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);
        RocketTripReturn tripReturn = new RocketTripReturn();
        tripReturn.setId(1L);
        tripReturn.setRocket(rocket);

        when(rocketTripReturnRepository.findById(1L)).thenReturn(java.util.Optional.of(tripReturn));

        RocketTripReturn result = rocketTripReturnService.findById(1L);

        assertEquals(tripReturn, result);
    }

    @Test
    void findById_throwsIfNotFound() {
        when(rocketTripReturnRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripReturnService.findById(1L));
        assertEquals("Could not find rocket trip for return with id: 1", ex.getMessage());
    }

    @Test
    void deleteAll_delegatesToRepository() {
        rocketTripReturnService.deleteAll();
        verify(rocketTripReturnRepository).deleteAll();
    }

    @Test
    void findReturnTripBySeatsAndTime_returnsTrips() {
        Rocket rocket = new Rocket();
        rocket.setSeatsTotal(5);

        RocketTripReturn trip1 = new RocketTripReturn();
        trip1.setId(1L);
        trip1.setRocket(rocket);
        trip1.setSeatsTaken(2);
        trip1.setReturnExactTime(LocalDateTime.now().plusDays(2));

        RocketTripReturn trip2 = new RocketTripReturn();
        trip2.setId(2L);
        trip2.setRocket(rocket);
        trip2.setSeatsTaken(1);
        trip2.setReturnExactTime(LocalDateTime.now().plusDays(3));

        when(rocketTripReturnRepository.findAll()).thenReturn(List.of(trip1, trip2));

        Map<Long, LocalDateTime> result = rocketTripReturnService.findReturnTripBySeatsAndTime(1);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
    }

    @Test
    void findReturnTripBySeatsAndTime_throwsIfNoTrips() {
        when(rocketTripReturnRepository.findAll()).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rocketTripReturnService.findReturnTripBySeatsAndTime(1));
        assertEquals("No trips found for return", ex.getMessage());
    }

    @Test
    void findByIdForUpdate_delegatesToRepository() {
        RocketTripReturn tripReturn = new RocketTripReturn();
        when(rocketTripReturnRepository.findByIdForUpdate(1L)).thenReturn(tripReturn);

        RocketTripReturn result = rocketTripReturnService.findByIdForUpdate(1L);

        assertEquals(tripReturn, result);
    }

    @Test
    void save_delegatesToRepository() {
        RocketTripReturn tripReturn = new RocketTripReturn();
        rocketTripReturnService.save(tripReturn);
        verify(rocketTripReturnRepository).save(tripReturn);
    }

    @Test
    void delete_delegatesToRepository() {
        RocketTripReturn tripReturn = new RocketTripReturn();
        rocketTripReturnService.delete(tripReturn);
        verify(rocketTripReturnRepository).delete(tripReturn);
    }

    @Test
    void findAll_returnsResponseList() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);

        RocketTripReturn tripReturn = new RocketTripReturn();
        tripReturn.setId(1L);
        tripReturn.setRocket(rocket);
        tripReturn.setReturnExactTime(LocalDateTime.now().plusDays(2));
        tripReturn.setSeatsTaken(2);

        when(rocketTripReturnRepository.findAll()).thenReturn(List.of(tripReturn));

        List<RocketTripReturnResponse> responses = rocketTripReturnService.findAll();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(1L, responses.get(0).getRocketId());
    }

    @Test
    void findByReturnExactTime_delegatesToRepository() {
        LocalDateTime time = LocalDateTime.now().plusDays(2);
        RocketTripReturn tripReturn = new RocketTripReturn();
        when(rocketTripReturnRepository.findByReturnExactTime(time)).thenReturn(tripReturn);

        RocketTripReturn result = rocketTripReturnService.findByReturnExactTime(time);

        assertEquals(tripReturn, result);
    }
}
