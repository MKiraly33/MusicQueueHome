package com.mate.kiraly.model;

import lombok.*;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    private String token;
    private LocalDateTime expireAt;
}
