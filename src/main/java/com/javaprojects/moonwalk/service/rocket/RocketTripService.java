package com.javaprojects.moonwalk.service.rocket;

import com.javaprojects.moonwalk.model.rocket.*;
import com.javaprojects.moonwalk.repository.RocketTripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RocketTripService {
    private final RocketTripRepository rocketTripRepository;
    private final RocketService rocketService;
    private final RocketTripReturnService rocketTripReturnService;

    public RocketTrip findById(Long id) {
        return rocketTripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find rocket trip with id: " + id));
    }

    public void deleteAll(){
        rocketTripRepository.deleteAll();
    }

    @Transactional
    public void create(RocketTripCreateRequest request) {
        Rocket rocket = rocketService.findById(request.getRocketId());
        if(rocket == null) {
            throw new RuntimeException("Rocket not found");
        }
        LocalDateTime time = request.getLaunchExactTime();
        if(time == null) {
            throw new RuntimeException("Launch exact time not found");
        }
        if(time.isBefore(LocalDateTime.now().plusDays(7))) {
            throw new RuntimeException("Launch exact time too early");
        }
        RocketTrip trip = new RocketTrip();
        trip.setRocket(rocket);
        trip.setLaunchExactTime(time);
        trip.setSeatsTaken(0);

        RocketTripReturn returnTrip = new RocketTripReturn();
        returnTrip.setRocket(rocket);
        returnTrip.setReturnExactTime(time.plusHours(3));
        returnTrip.setSeatsTaken(0);

        rocketTripReturnService.save(returnTrip);
        rocketTripRepository.save(trip);
    }

    public List<RocketTripResponse> findAll(){
        List<RocketTrip> rocketTrips = rocketTripRepository.findAll();
        List<RocketTripResponse> rocketTripResponses = new ArrayList<>();
        for(RocketTrip rocketTrip : rocketTrips) {
            RocketTripResponse response = RocketTripResponse
                    .builder()
                    .id(rocketTrip.getId())
                    .launchExactTime(rocketTrip.getLaunchExactTime())
                    .seatsTaken(rocketTrip.getSeatsTaken())
                    .rocketId(rocketTrip.getRocket().getId())
                    .build();
            rocketTripResponses.add(response);
        }
        return rocketTripResponses;
    }

    public Map<Long, LocalDateTime> findTripBySeatsAndTime(int numPassengers) {
        Map<Long, LocalDateTime> correspondingTrips = new HashMap<>();
        for(RocketTrip trip : rocketTripRepository.findAll()) {
            if(trip.getSeatsTaken() < trip.getRocket().getSeatsTotal() - numPassengers
            && trip.getLaunchExactTime().isAfter(LocalDateTime.now().plusDays(1))) {
                correspondingTrips.put(trip.getId(), trip.getLaunchExactTime());
            }
        }
        if(correspondingTrips.isEmpty()) {
            throw new RuntimeException("No trips found");
        }
        return correspondingTrips;
    }

    public RocketTrip findByIdForUpdate(Long id) {
        return rocketTripRepository.findByIdForUpdate(id);
    }
    public RocketTrip save(RocketTrip trip) {
        return rocketTripRepository.save(trip);
    }
}
