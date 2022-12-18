package com.mate.kiraly.service;

import com.mate.kiraly.dto.AddTrackDTO;
import com.mate.kiraly.dto.RoomDTO;
import com.mate.kiraly.dto.RoomListDTO;

import java.util.List;

public interface RoomService {
    List<RoomListDTO> getAllRooms();
    RoomDTO getRoomData(Long id);
    boolean registerRoom(String name, Long masterUserId);
    boolean joinRoom(Long roomId, Long userId);

    Boolean addTrack(AddTrackDTO addTrackDTO);
}
