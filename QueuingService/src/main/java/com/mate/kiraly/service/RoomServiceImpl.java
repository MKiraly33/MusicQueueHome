package com.mate.kiraly.service;

import com.mate.kiraly.dto.AddTrackDTO;
import com.mate.kiraly.dto.RoomDTO;
import com.mate.kiraly.dto.RoomListDTO;
import com.mate.kiraly.model.Room;
import com.mate.kiraly.repository.QueuingRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final QueuingRepo queuingRepo;
    @Autowired
    private final WebClient.Builder webClientBuilder;
    public List<RoomListDTO> getAllRooms() {
        List<Room> allRooms = queuingRepo.findAll();
        ArrayList<RoomListDTO> rooms = new ArrayList<>();
        if(allRooms.size() > 0){
            for(Room r : allRooms){
                RoomListDTO rDTO = new RoomListDTO();
                rDTO.setId(r.getRoomId());
                rDTO.setName(r.getName());
                rooms.add(rDTO);
            }
        }
        return rooms;
    }

    public RoomDTO getRoomData(Long id){
        RoomDTO rDTO = new RoomDTO();
        Optional<Room> room = queuingRepo.findById(id);

        if(room.isPresent()){
            Room r = room.get();
            rDTO.setRoomId(id);
            rDTO.setName(r.getName());
            rDTO.setUserIds(r.getUserIds());
            rDTO.setMasterUserId(r.getMasterUserId());
            rDTO.setTracksInQueue(r.getTracksInQueue());
        }
        return rDTO;
    }

    public boolean registerRoom(String name, Long masterUserId){
        Room r = new Room();
        r.setName(name);
        r.setMasterUserId(masterUserId);
        queuingRepo.save(r);
        return true;
    }

    public boolean joinRoom(Long roomId, Long userId){
        Optional<Room> room = queuingRepo.findById(roomId);
        if(room.isPresent()){
            Room r = room.get();
            if(!r.getUserIds().contains(userId)){
                r.getUserIds().add(userId);
                queuingRepo.save(r);
                return true;
            }
        }
        return false;
    }

    public Boolean isUserInRoom(Long userId, Long roomId){
        RoomDTO roomDTO = getRoomData(roomId);
        if(roomDTO.getRoomId() != null){
            if(roomDTO.getUserIds().contains(userId) || roomDTO.getMasterUserId().equals(userId)){
                return true;
            }
        }
        return false;
    }

    public Boolean addTrack(AddTrackDTO addTrackDTO) {
        if(isUserInRoom(addTrackDTO.getUserId(), addTrackDTO.getRoomId())){
            RoomDTO roomDTO = getRoomData(addTrackDTO.getRoomId());
            Room r = new Room();
            r.setRoomId(roomDTO.getRoomId());
            r.setUserIds(roomDTO.getUserIds());
            r.setName(roomDTO.getName());
            r.setMasterUserId(roomDTO.getMasterUserId());
            String href = webClientBuilder.build().get().uri("http://DataFetchingService/api/fetch/track?artist=" + addTrackDTO.getArtist() +
                    "&name=" + addTrackDTO.getSong()).retrieve().bodyToMono(String.class).block();
            roomDTO.getTracksInQueue().add(href);
            r.setTracksInQueue(roomDTO.getTracksInQueue());
        }
        return false;
    }
}
