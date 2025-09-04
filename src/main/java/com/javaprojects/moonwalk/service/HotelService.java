package com.javaprojects.moonwalk.service;

import com.javaprojects.moonwalk.model.hotel.Hotel;
import com.javaprojects.moonwalk.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;

    public Hotel save(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public void deleteAll(){
        hotelRepository.deleteAll();
    }

    public Map<Long, String> findAll() {
        List<Hotel> hotels = hotelRepository.findAll();
        Map<Long, String> hotelMap = new HashMap<>();
        for(Hotel hotel : hotels) {
            hotelMap.put(hotel.getId(), hotel.getName());
        }
        return hotelMap;
    }

    public Hotel findById(Long hotelId) {
        return hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    public Hotel findByEmail(String email) {
        return hotelRepository.findByEmail(email);
    }
}
