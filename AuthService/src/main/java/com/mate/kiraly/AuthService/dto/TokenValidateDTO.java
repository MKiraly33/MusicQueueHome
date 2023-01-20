package com.mate.kiraly.AuthService.dto;

import lombok.Data;

@Data
public class TokenValidateDTO {
    private String message;
    private Boolean isValid;
    private Long userId;
}
