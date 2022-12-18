package com.mate.kiraly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotyTokenResponse {

    private String access_token;
    private String token_type;
    private Integer expires_in;
}
