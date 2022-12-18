package com.mate.kiraly.repository;

import com.mate.kiraly.model.Token;
import com.mate.kiraly.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DataFetchingRepo extends JpaRepository<Track,Long> {
    Optional<Track> findByArtistAndName(String artist, String name);
}
