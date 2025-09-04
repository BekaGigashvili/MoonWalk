package com.javaprojects.moonwalk.model.room;

import com.javaprojects.moonwalk.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToMany
    @JoinTable(
            name = "room_booking_rooms",
            joinColumns = @JoinColumn(name = "room_booking_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private List<Room> rooms = new ArrayList<>();
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
}