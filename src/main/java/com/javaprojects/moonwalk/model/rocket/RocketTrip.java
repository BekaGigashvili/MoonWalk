package com.javaprojects.moonwalk.model.rocket;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RocketTrip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDateTime launchExactTime;
    @ManyToOne
    @JoinColumn(name = "rocket_id")
    private Rocket rocket;
    private int seatsTaken;
    @OneToMany(mappedBy = "rocketTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RocketTripBooking> tripUsers = new ArrayList<>();
}