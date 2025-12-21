package ru.stellarburgers.api.models;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private User user;
    private String message;
}