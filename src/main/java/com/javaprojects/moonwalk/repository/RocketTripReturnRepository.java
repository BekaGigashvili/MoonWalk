package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.rocket.RocketTripReturn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RocketTripReturnRepository extends JpaRepository<RocketTripReturn, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RocketTripReturn r WHERE r.id = :id")
    RocketTripReturn findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT r FROM RocketTripReturn r WHERE r.returnExactTime = :returnExactTime")
    RocketTripReturn findByReturnExactTime(LocalDateTime returnExactTime);
}
