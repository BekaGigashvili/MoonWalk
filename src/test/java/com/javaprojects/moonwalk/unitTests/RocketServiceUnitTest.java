package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.repository.RocketRepository;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RocketServiceUnitTest {

    private RocketRepository rocketRepository;
    private RocketService rocketService;

    @BeforeEach
    void setUp() {
        rocketRepository = mock(RocketRepository.class);
        rocketService = new RocketService(rocketRepository);
    }

    @Test
    void findById_returnsRocket() {
        Rocket rocket = new Rocket();
        rocket.setId(1L);

        when(rocketRepository.findById(1L)).thenReturn(Optional.of(rocket));

        Rocket result = rocketService.findById(1L);

        assertEquals(rocket, result);
    }

    @Test
    void findById_throwsIfNotFound() {
        when(rocketRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> rocketService.findById(1L));
        assertEquals("Could not find rocket with id 1", ex.getMessage());
    }

    @Test
    void findAll_returnsAllRockets() {
        Rocket rocket1 = new Rocket();
        Rocket rocket2 = new Rocket();

        when(rocketRepository.findAll()).thenReturn(List.of(rocket1, rocket2));

        List<Rocket> rockets = rocketService.findAll();

        assertEquals(2, rockets.size());
        verify(rocketRepository).findAll();
    }

    @Test
    void addRocket_savesRocket_whenNoConflict() {
        Rocket rocket = new Rocket();
        rocket.setLaunchDay(WeekDay.MONDAY);
        rocket.setLaunchTime("10");

        when(rocketRepository.findAllByLaunchDay(WeekDay.MONDAY)).thenReturn(List.of());

        rocketService.addRocket(rocket);

        verify(rocketRepository).save(rocket);
    }

    @Test
    void addRocket_throwsIfConflict() {
        Rocket existing = new Rocket();
        existing.setLaunchDay(WeekDay.MONDAY);
        existing.setLaunchTime("10");

        Rocket rocket = new Rocket();
        rocket.setLaunchDay(WeekDay.MONDAY);
        rocket.setLaunchTime("10");

        when(rocketRepository.findAllByLaunchDay(WeekDay.MONDAY)).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> rocketService.addRocket(rocket));
        assertEquals("There is a rocket with the same launch day and time", ex.getMessage());
    }

    @Test
    void save_delegatesToRepository() {
        Rocket rocket = new Rocket();
        when(rocketRepository.save(rocket)).thenReturn(rocket);

        Rocket result = rocketService.save(rocket);

        assertEquals(rocket, result);
        verify(rocketRepository).save(rocket);
    }

    @Test
    void deleteAll_delegatesToRepository() {
        rocketService.deleteAll();
        verify(rocketRepository).deleteAll();
    }
}
