package com.javaprojects.moonwalk.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRoomRequest {
    private int roomNumber;
    private int capacity;
    private Double price;
}
