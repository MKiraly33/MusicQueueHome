package com.mate.kiraly.controller;

import com.mate.kiraly.dto.*;
import com.mate.kiraly.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/")
    public List<RoomListDTO> getAllRooms(){
        return roomService.getAllRooms();
    }

    @GetMapping("/{roomId}")
    public RoomDTO getRoomData(@PathVariable Long roomId)
    {
        return roomService.getRoomData(roomId);
    }

    @PostMapping("/join")
    public ResultFieldDTO joinRoom(@RequestBody RoomJoinDTO roomJoinDTO){
        return roomService.joinRoom(roomJoinDTO.getRoomId(), roomJoinDTO.getUserId());
    }

    @PostMapping("/register")
    public ResultFieldDTO registerRoom(@RequestBody RoomRegisterDTO roomRegisterDTO){
        return roomService.registerRoom(roomRegisterDTO.getRoomName(), roomRegisterDTO.getUserId());
    }
    @PostMapping("/addtrack")
    public ResultFieldDTO addTrack(@RequestBody AddTrackDTO addTrackDTO){
        return roomService.addTrack(addTrackDTO);
    }

    @DeleteMapping("/consume/{roomId}")
    public String consume(@PathVariable("roomId") Long roomId){ return roomService.consume(roomId);}

}
