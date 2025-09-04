package com.javaprojects.moonwalk.model.room;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.hotel.Hotel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int roomNumber;
    private int capacity;
    private Double price;
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
}
