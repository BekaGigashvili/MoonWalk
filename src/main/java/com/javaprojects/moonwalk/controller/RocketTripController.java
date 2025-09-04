package com.javaprojects.moonwalk.controller;

import com.javaprojects.moonwalk.model.rocket.RocketTripCreateRequest;
import com.javaprojects.moonwalk.model.rocket.RocketTripResponse;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import com.javaprojects.moonwalk.model.rocket.RocketTripReturnResponse;
import com.javaprojects.moonwalk.service.rocket.RocketTripReturnService;
import com.javaprojects.moonwalk.service.rocket.RocketTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trip")
@RequiredArgsConstructor
public class RocketTripController {
    private final RocketTripService rocketTripService;
    private final RocketTripReturnService rocketTripReturnService;

    @PostMapping("/create")
    public void create(@RequestBody RocketTripCreateRequest request) {
        rocketTripService.create(request);
    }

    @GetMapping("/earth-moon")
    public List<RocketTripResponse> findAllEarthToMoon(){
        return rocketTripService.findAll();
    }
    @GetMapping("/moon-earth")
    public List<RocketTripReturnResponse> findAllMoonToEarth(){
        return rocketTripReturnService.findAll();
    }

    @GetMapping("/launch/{numPassengers}")
    public Map<Long, LocalDateTime> findCorrespondingTrips(@PathVariable int numPassengers) {
        return rocketTripService.findTripBySeatsAndTime(numPassengers);
    }

    @GetMapping("/return/{numPassengers}")
    public Map<Long, LocalDateTime> findCorrespondingReturnTrips(@PathVariable int numPassengers) {
        return rocketTripReturnService.findReturnTripBySeatsAndTime(numPassengers);
    }
}
