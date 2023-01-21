package com.mate.kiraly.service;

import com.mate.kiraly.dto.AddTrackDTO;
import com.mate.kiraly.dto.ResultFieldDTO;
import com.mate.kiraly.dto.RoomDTO;
import com.mate.kiraly.dto.RoomListDTO;

import java.util.List;

public interface RoomService {
    List<RoomListDTO> getAllRooms();
    RoomDTO getRoomData(Long id);
    ResultFieldDTO registerRoom(String name, Long masterUserId, String uid);
    ResultFieldDTO joinRoom(Long roomId, String uid);

    ResultFieldDTO addTrack(AddTrackDTO addTrackDTO, String uid);

    String consume(Long roomId, String uid);
    ResultFieldDTO leaveRoom(Long roomId, String uid);
}
