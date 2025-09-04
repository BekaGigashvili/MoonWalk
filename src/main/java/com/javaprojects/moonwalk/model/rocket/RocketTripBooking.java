package com.javaprojects.moonwalk.model.rocket;

import com.javaprojects.moonwalk.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RocketTripBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rocketTrip_id")
    private RocketTrip rocketTrip;

    @ManyToOne
    @JoinColumn(name = "return_trip_id")
    private RocketTripReturn returnTrip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String seatNumbers;
    private String returnSeatNumbers;
}

