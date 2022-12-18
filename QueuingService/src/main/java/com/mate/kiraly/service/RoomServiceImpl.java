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
                    if(o1.getPlaceInQueue() < o2.getPlaceInQueue()){
                        return -1;
                    }else if(o1.getPlaceInQueue() > o2.getPlaceInQueue()){
                        return 1;
                    }else{
                        return 0;
                    }
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

    public ResultFieldDTO registerRoom(String name, Long masterUserId){
        Room r = new Room();
        r.setName(name);
        r.setMasterUserId(masterUserId);
        queuingRepo.save(r);
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        resultFieldDTO.setResult("Room registration successful");
        return resultFieldDTO;
    }

    public ResultFieldDTO joinRoom(Long roomId, Long userId){
        Optional<Room> room = queuingRepo.findById(roomId);
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        if(room.isPresent()){
            Room r = room.get();
            if(!r.getUserIds().contains(userId)){
                r.getUserIds().add(userId);
                queuingRepo.save(r);
                resultFieldDTO.setResult("Room joined successfully");
                return resultFieldDTO;
            }
        }
        resultFieldDTO.setResult("Room does not exist");
        return resultFieldDTO;
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

    public ResultFieldDTO addTrack(AddTrackDTO addTrackDTO) {
        ResultFieldDTO resultFieldDTO = new ResultFieldDTO();
        if(isUserInRoom(addTrackDTO.getUserId(), addTrackDTO.getRoomId())){
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

    public String consume(Long roomId){

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
