package com.mate.kiraly.repository;

import com.mate.kiraly.model.TrackQueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrackQueueItemRepo extends JpaRepository<TrackQueueItem, Long> {
    @Query(value = "SELECT MAX(place_in_queue) FROM track_queue_item WHERE room_id = ?1", nativeQuery = true)
    Long findMaxPositionInQueueByRoomId(Long roomId);

    @Query(value = "SELECT MIN(place_in_queue) FROM track_queue_item WHERE room_id = ?1", nativeQuery = true)
    Long findMinPositionInQueueByRoomId(Long roomId);
    Optional<TrackQueueItem> findByRoomIdAndPlaceInQueue(Long roomId, Long placeInQueue);

    List<TrackQueueItem> findByRoomId(Long roomId);
}
