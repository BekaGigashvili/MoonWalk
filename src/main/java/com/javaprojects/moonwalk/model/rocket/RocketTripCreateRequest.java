package com.javaprojects.moonwalk.model.rocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RocketTripCreateRequest {
    private LocalDateTime launchExactTime;
    private Long rocketId;
}