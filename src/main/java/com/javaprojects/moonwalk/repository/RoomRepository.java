package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.RoomStatus;
import com.javaprojects.moonwalk.model.room.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findRoomsByHotelId(Long hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.capacity = :capacity")
    List<Room> findRoomsByHotelIdAndCapacityForUpdate(
            @Param("hotelId") Long hotelId,
            @Param("capacity") int capacity
    );

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.roomNumber = :roomNumber")
    Room findRoomByHotelIdAndRoomNumber(Long hotelId, int roomNumber);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.status = :status")
    List<Room> findRoomsByHotelIdAndStatus(Long hotelId, RoomStatus status);
}
