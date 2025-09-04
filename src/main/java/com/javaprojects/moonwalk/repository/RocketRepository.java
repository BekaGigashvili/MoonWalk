package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.WeekDay;
import com.javaprojects.moonwalk.model.rocket.Rocket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RocketRepository extends JpaRepository<Rocket, Long> {
    @Query("SELECT r FROM Rocket r WHERE r.launchDay = :launchDay")
    List<Rocket> findAllByLaunchDay(WeekDay launchDay);
}
