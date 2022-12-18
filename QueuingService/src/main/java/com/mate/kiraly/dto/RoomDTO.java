package com.mate.kiraly.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class RoomDTO {
    private Long roomId;
    private String name;
    private List<String> tracksInQueue;
    private List<Long> userIds;
    private Long masterUserId;
}
