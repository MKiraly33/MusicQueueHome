package com.mate.kiraly.repository;

import com.mate.kiraly.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QueuingRepo extends JpaRepository<Room, Long> {
    Optional<Room> findByName(String name);
    @Query(value = "SELECT COUNT(*) FROM room_user_ids WHERE user_ids = ?1", nativeQuery = true)
    Integer getRoomCountUserAlreadyIn(Long userId);
}
