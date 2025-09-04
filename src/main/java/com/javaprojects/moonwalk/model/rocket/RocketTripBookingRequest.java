package com.javaprojects.moonwalk.model.rocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RocketTripBookingRequest {
    private Long tripId;
    private Long returnTripId;
    private int numPassengers;
}
