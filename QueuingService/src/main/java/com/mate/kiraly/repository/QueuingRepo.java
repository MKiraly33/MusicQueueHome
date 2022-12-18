package com.mate.kiraly.repository;

import com.mate.kiraly.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QueuingRepo extends JpaRepository<Room, Long> {
    Optional<Room> findByName(String name);
}
