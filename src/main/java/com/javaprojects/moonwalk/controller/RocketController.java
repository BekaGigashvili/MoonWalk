package com.javaprojects.moonwalk.controller;

import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.service.rocket.RocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rocket")
public class RocketController {
    private final RocketService rocketService;

    @GetMapping
    public List<Rocket> findAll(){
        return rocketService.findAll();
    }

    @PostMapping
    public void addRocket(@RequestBody Rocket rocket) {
        rocketService.addRocket(rocket);
    }
}
