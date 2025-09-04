package com.javaprojects.moonwalk.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {
    private int capacity;
    private int quantity;
}
