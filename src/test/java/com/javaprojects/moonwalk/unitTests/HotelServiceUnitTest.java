package com.javaprojects.moonwalk.unitTests;

import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.repository.HotelRepository;
import com.javaprojects.moonwalk.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HotelServiceUnitTest {

    private HotelRepository hotelRepository;
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        hotelRepository = mock(HotelRepository.class);
        hotelService = new HotelService(hotelRepository);
    }

    @Test
    void save_callsRepositorySave() {
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");

        hotelService.save(hotel);

        verify(hotelRepository).save(hotel);
    }

    @Test
    void deleteAll_callsRepositoryDeleteAll() {
        hotelService.deleteAll();
        verify(hotelRepository).deleteAll();
    }

    @Test
    void findAll_returnsMapOfHotels() {
        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("Hotel A");

        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("Hotel B");

        when(hotelRepository.findAll()).thenReturn(List.of(hotel1, hotel2));

        Map<Long, String> result = hotelService.findAll();

        assertEquals(2, result.size());
        assertEquals("Hotel A", result.get(1L));
        assertEquals("Hotel B", result.get(2L));

        verify(hotelRepository).findAll();
    }

    @Test
    void findById_existingHotel_returnsHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Hotel A");

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        Hotel result = hotelService.findById(1L);

        assertEquals(hotel, result);
        verify(hotelRepository).findById(1L);
    }

    @Test
    void findById_nonExistingHotel_throwsException() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            hotelService.findById(1L);
        });

        assertEquals("Hotel not found", exception.getMessage());
        verify(hotelRepository).findById(1L);
    }

    @Test
    void findByEmail_callsRepositoryFindByEmail() {
        Hotel hotel = new Hotel();
        hotel.setEmail("test@hotel.com");

        when(hotelRepository.findByEmail("test@hotel.com")).thenReturn(hotel);

        Hotel result = hotelService.findByEmail("test@hotel.com");

        assertEquals(hotel, result);
        verify(hotelRepository).findByEmail("test@hotel.com");
    }
}
