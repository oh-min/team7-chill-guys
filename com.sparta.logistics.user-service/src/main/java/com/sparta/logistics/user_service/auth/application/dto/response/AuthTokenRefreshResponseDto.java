package com.sparta.logistics.user_service.auth.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenRefreshResponseDto {

    private String newAccessToken;
    private String newRefreshToken;

}
