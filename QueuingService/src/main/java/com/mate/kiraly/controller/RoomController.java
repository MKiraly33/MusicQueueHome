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
    public ResultFieldDTO joinRoom(@RequestBody RoomJoinDTO roomJoinDTO, @RequestHeader("auth-user-id") String uid){
        return roomService.joinRoom(roomJoinDTO.getRoomId(), uid);
    }

    @PostMapping("/register")
    public ResultFieldDTO registerRoom(@RequestBody RoomRegisterDTO roomRegisterDTO, @RequestHeader("auth-user-id") String uid){
        return roomService.registerRoom(roomRegisterDTO.getRoomName(), roomRegisterDTO.getUserId(), uid);
    }
    @PostMapping("/addtrack")
    public ResultFieldDTO addTrack(@RequestBody AddTrackDTO addTrackDTO, @RequestHeader("auth-user-id") String uid){
        return roomService.addTrack(addTrackDTO, uid);
    }

    @DeleteMapping("/consume/{roomId}")
    public String consume(@PathVariable("roomId") Long roomId, @RequestHeader("auth-user-id") String uid){
        return roomService.consume(roomId, uid);}

    @PostMapping("/leave")
    public ResultFieldDTO leaveRoom(@RequestBody RoomJoinDTO roomLeaveDTO, @RequestHeader("auth-user-id") String uid){
        return roomService.leaveRoom(roomLeaveDTO.getRoomId(), uid);
    }
}
