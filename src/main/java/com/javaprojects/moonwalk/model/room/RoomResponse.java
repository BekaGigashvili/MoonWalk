package com.javaprojects.moonwalk.model.room;

import com.javaprojects.moonwalk.model.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomResponse {
    private int roomNumber;
    private int capacity;
    private Double price;
    private RoomStatus status;
}
