package com.javaprojects.moonwalk.model.room;

import com.javaprojects.moonwalk.model.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetStatusRequest {
    private int roomNumber;
    private RoomStatus status;
}
