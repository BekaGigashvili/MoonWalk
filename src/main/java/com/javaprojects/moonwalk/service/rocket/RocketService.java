package com.javaprojects.moonwalk.service.rocket;

import com.javaprojects.moonwalk.model.rocket.Rocket;
import com.javaprojects.moonwalk.repository.RocketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RocketService {
    private final RocketRepository rocketRepository;

    public Rocket findById(Long id) {
        return rocketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find rocket with id " + id));
    }

    public List<Rocket> findAll(){
        return rocketRepository.findAll();
    }

    public void addRocket(Rocket rocket) {
        List<Rocket> rockets = rocketRepository.findAllByLaunchDay(rocket.getLaunchDay());
        for(Rocket rocket2 : rockets) {
            if(Objects.equals(rocket2.getLaunchTime(), rocket.getLaunchTime())) {
                throw new RuntimeException("There is a rocket with the same launch day and time");
            }
        }
        rocketRepository.save(rocket);
    }

    public Rocket save(Rocket rocket) {
        return rocketRepository.save(rocket);
    }

    public void deleteAll(){
        rocketRepository.deleteAll();
    }
}
