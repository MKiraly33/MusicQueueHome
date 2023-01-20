package com.mate.kiraly.dto;

import org.springframework.stereotype.Component;

@Component
public class TokenValidateDTO {
    private String message;
    private Boolean isValid;
    private Long userId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
