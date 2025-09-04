package com.javaprojects.moonwalk.model.rocket;

import com.javaprojects.moonwalk.model.WeekDay;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Rocket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private WeekDay launchDay;
    private String launchTime;
    private int seatsTotal;
    private double price;
}
