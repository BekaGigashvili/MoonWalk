package com.javaprojects.moonwalk.model.hotel;

import com.javaprojects.moonwalk.model.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String email;
    private String description;
    private Double rating;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hotel")
    private List<Room> rooms = new ArrayList<>();
}
