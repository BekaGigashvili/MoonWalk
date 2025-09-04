package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.rocket.RocketTripBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RocketTripBookingRepository extends JpaRepository<RocketTripBooking, Long> {
}
