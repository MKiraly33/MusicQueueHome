package com.mate.kiraly.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;
    private String name;
    @ElementCollection(targetClass = String.class)
    private List<String> tracksInQueue;
    @ElementCollection(targetClass = Long.class)
    private List<Long> userIds;
    private Long masterUserId;
}
