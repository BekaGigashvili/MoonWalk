package com.javaprojects.moonwalk.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomBookingRequest {
    private Long hotelId;
    private List<RoomRequest> rooms;
    private Long tripId;
    private Long returnTripId;
}
