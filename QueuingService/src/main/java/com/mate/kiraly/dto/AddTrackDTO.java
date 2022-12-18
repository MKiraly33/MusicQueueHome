package com.mate.kiraly.dto;

import lombok.Data;

@Data
public class AddTrackDTO {
    private Long roomId;
    private Long userId;
    private String artist;
    private String song;
}
