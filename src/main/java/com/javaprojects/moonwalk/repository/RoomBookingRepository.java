package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.room.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
    @Query("""
                SELECT CASE WHEN COUNT(rb) > 0 THEN true ELSE false END
                FROM RoomBooking rb
                JOIN rb.rooms r
                WHERE r.id = :roomId
                  AND rb.checkOutDate > :checkInDate
                  AND rb.checkInDate < :checkOutDate
            """)
    boolean existsByRoomAndDateOverlap(@Param("roomId") Long roomId,
                                       @Param("checkInDate") LocalDateTime checkInDate,
                                       @Param("checkOutDate") LocalDateTime checkOutDate);
}
