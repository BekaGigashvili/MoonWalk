package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.rocket.RocketTrip;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RocketTripRepository extends JpaRepository<RocketTrip, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RocketTrip r WHERE r.id = :id")
    RocketTrip findByIdForUpdate(@Param("id") Long id);
}
