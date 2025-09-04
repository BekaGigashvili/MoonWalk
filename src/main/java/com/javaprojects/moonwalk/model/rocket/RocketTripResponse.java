package com.javaprojects.moonwalk.model.rocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RocketTripResponse {
    private Long id;
    private LocalDateTime launchExactTime;
    private Long rocketId;
    private int seatsTaken;
}
