package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.hotel.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Query("SELECT h FROM Hotel h WHERE h.email = :email")
    Hotel findByEmail(String email);
}
