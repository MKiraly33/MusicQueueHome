package com.mate.kiraly.service;

import com.mate.kiraly.dto.AddTrackDTO;
import com.mate.kiraly.dto.ResultFieldDTO;
import com.mate.kiraly.dto.RoomDTO;
import com.mate.kiraly.dto.RoomListDTO;
import com.mate.kiraly.model.Room;
import com.mate.kiraly.model.TrackQueueItem;
import com.mate.kiraly.repository.QueuingRepo;
import com.mate.kiraly.repository.TrackQueueItemRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final QueuingRepo queuingRepo;
    private final TrackQueueItemRepo trackQueueItemRepo;
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
            List<TrackQueueItem> trackQueueItems = trackQueueItemRepo.findByRoomId(r.getRoomId());
            trackQueueItems.sort(new Comparator<TrackQueueItem>() {
                @Override
                public int compare(TrackQueueItem o1, TrackQueueItem o2) {
                    return o1.getPlaceInQueue().compareTo(o2.getPlaceInQueue());
                }
            });
            List<String> hrefs = new ArrayList<>();
            for(TrackQueueItem item : trackQueueItems){
                hrefs.add(item.getSpotyLink());
            }
            rDTO.setTracksInQueue(hrefs);
        }
        return rDTO;
    }

    public ResultFieldDTO registerRoom(String name, Long masterUserId, String uid){
        Long userId = Long.parseLong(uid);
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        if(queuingRepo.getRoomCountUserAlreadyIn(userId) > 0){
            resultFieldDTO.setResult("User already inside a room, can not create a new one");
            return resultFieldDTO;
        }
        Room r = new Room();
        r.setName(name);
        r.setMasterUserId(masterUserId);
        queuingRepo.save(r);

        resultFieldDTO.setResult("Room registration successful");
        return resultFieldDTO;
    }

    public ResultFieldDTO joinRoom(Long roomId, String uid){
        Long userId = Long.parseLong(uid);
        Optional<Room> room = queuingRepo.findById(roomId);
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        if(room.isPresent()){
            Room r = room.get();
            if(queuingRepo.getRoomCountUserAlreadyIn(userId) > 0){
                resultFieldDTO.setResult("User already in a room, can not join another");
            }else{
                r.getUserIds().add(userId);
                queuingRepo.save(r);
                resultFieldDTO.setResult("Joined room successfully");
            }
            return resultFieldDTO;
        }
        resultFieldDTO.setResult("Room does not exist");
        return resultFieldDTO;
    }

    public ResultFieldDTO leaveRoom(Long roomId, String uid){
        Long userId = Long.parseLong(uid);
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        if(isUserInRoom(userId, roomId)){
            Optional<Room> optionalRoom = queuingRepo.findById(roomId);
            if(optionalRoom.isPresent()){
                Room r = optionalRoom.get();
                if(r.getUserIds().contains(userId)){ // If the leaving user is a user, delete from user list
                    r.getUserIds().remove(userId);
                    resultFieldDTO.setResult("Successfully removed user from room");
                }else{ // Otherwise at this point the leaving user must be the room master, so delete the room
                    queuingRepo.delete(r);
                    resultFieldDTO.setResult("User was room master, successfully deleted room");
                }
            }
        }else{
            resultFieldDTO.setResult("User is not in the specified room");
        }
        return resultFieldDTO;
    }

    public Boolean isUserInRoom(Long userId, Long roomId){
        RoomDTO roomDTO = getRoomData(roomId);
        if(roomDTO.getRoomId() != null){
            return roomDTO.getUserIds().contains(userId) || roomDTO.getMasterUserId().equals(userId);
        }
        return false;
    }

    public ResultFieldDTO addTrack(AddTrackDTO addTrackDTO, String uid) {
        Long userId = Long.parseLong(uid);
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        if(isUserInRoom(userId, addTrackDTO.getRoomId())){
            RoomDTO roomDTO = getRoomData(addTrackDTO.getRoomId());
            Room r = new Room();
            r.setRoomId(roomDTO.getRoomId());
            r.setUserIds(roomDTO.getUserIds());
            r.setName(roomDTO.getName());
            r.setMasterUserId(roomDTO.getMasterUserId());
            r.setTracksInQueue(roomDTO.getTracksInQueue());
            String href = webClientBuilder.build().get().uri("http://DataFetchingService/api/fetch/track?artist=" + addTrackDTO.getArtist() +
                    "&name=" + addTrackDTO.getSong()).retrieve().bodyToMono(String.class).block();
            TrackQueueItem trackQueueItem = new TrackQueueItem();
            trackQueueItem.setSpotyLink(href);
            trackQueueItem.setRoomId(addTrackDTO.getRoomId());
            Long maxPos = trackQueueItemRepo.findMaxPositionInQueueByRoomId(addTrackDTO.getRoomId());
            if(maxPos == null){
                maxPos = 0L;
            }else{
                maxPos += 1L;
            }
            trackQueueItem.setPlaceInQueue(maxPos);
            queuingRepo.save(r);
            trackQueueItemRepo.save(trackQueueItem);
            resultFieldDTO.setResult("Track added successfully");
            return resultFieldDTO;
        }
        resultFieldDTO.setResult("User not in the specified room");
        return resultFieldDTO;
    }

    public String consume(Long roomId, String uid){
        if(!isUserInRoom(Long.parseLong(uid), roomId)){
            return "Consuming user is not in the specified room";
        }
        Long minQueuePos = trackQueueItemRepo.findMinPositionInQueueByRoomId(roomId);
        Optional<TrackQueueItem> optionalTrackQueueItem = trackQueueItemRepo.findByRoomIdAndPlaceInQueue(roomId, minQueuePos);

        if(optionalTrackQueueItem.isPresent()){
            trackQueueItemRepo.delete(optionalTrackQueueItem.get());
            return optionalTrackQueueItem.get().getSpotyLink();
        }else{
            return "Error";
        }
    }
}
