package com.javaprojects.moonwalk.service.rocket;

import com.javaprojects.moonwalk.model.rocket.RocketTrip;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturnResponse;
import com.javaprojects.moonwalk.repository.RocketTripReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RocketTripReturnService {
    private final RocketTripReturnRepository rocketTripReturnRepository;

    public RocketTripReturn findById(Long id) {
        return rocketTripReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find rocket trip for return with id: " + id));
    }

    public void deleteAll(){
        rocketTripReturnRepository.deleteAll();
    }

    public Map<Long, LocalDateTime> findReturnTripBySeatsAndTime(int numPassengers) {
        Map<Long, LocalDateTime> correspondingTrips = new HashMap<>();
        for(RocketTripReturn trip : rocketTripReturnRepository.findAll()) {
            if(trip.getSeatsTaken() < trip.getRocket().getSeatsTotal() - numPassengers
                    && trip.getReturnExactTime().isAfter(LocalDateTime.now().plusDays(1))) {
                correspondingTrips.put(trip.getId(), trip.getReturnExactTime());
            }
        }
        if(correspondingTrips.isEmpty()) {
            throw new RuntimeException("No trips found for return");
        }
        return correspondingTrips;
    }

    public RocketTripReturn findByIdForUpdate(Long id) {
        return rocketTripReturnRepository.findByIdForUpdate(id);
    }

    public RocketTripReturn save(RocketTripReturn rocketTripReturn) {
        return rocketTripReturnRepository.save(rocketTripReturn);
    }

    public void delete(RocketTripReturn rocketTripReturn) {
        rocketTripReturnRepository.delete(rocketTripReturn);
    }
    public List<RocketTripReturnResponse> findAll(){
        List<RocketTripReturn> rocketTripReturns = rocketTripReturnRepository.findAll();
        List<RocketTripReturnResponse> responses = new ArrayList<>();
        for(RocketTripReturn rocketTripReturn : rocketTripReturns) {
            RocketTripReturnResponse response = RocketTripReturnResponse
                    .builder()
                    .id(rocketTripReturn.getId())
                    .rocketId(rocketTripReturn.getRocket().getId())
                    .returnExactTime(rocketTripReturn.getReturnExactTime())
                    .seatsTaken(rocketTripReturn.getSeatsTaken())
                    .build();
            responses.add(response);
        }
        return responses;
    }

    public RocketTripReturn findByReturnExactTime(LocalDateTime localDateTime) {
        return rocketTripReturnRepository.findByReturnExactTime(localDateTime);
    }
}
