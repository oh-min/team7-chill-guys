package com.sparta.logistics.user_service.auth.application.dto.request;

import lombok.Getter;

@Getter
public class AuthLoginRequestDto {

    private String username;
    private String password;

}
