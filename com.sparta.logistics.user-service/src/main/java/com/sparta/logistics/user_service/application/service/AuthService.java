package com.sparta.logistics.user_service.application.service;


import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;
import com.sparta.logistics.user_service.application.dto.request.AuthLoginRequestDto;
import com.sparta.logistics.user_service.application.dto.request.AuthSignupRequestDto;
import com.sparta.logistics.user_service.application.dto.response.AuthSignupResponseDto;
import com.sparta.logistics.user_service.application.dto.response.AuthTokenResponseDto;
import com.sparta.logistics.user_service.domain.entity.User;
import com.sparta.logistics.user_service.domain.entity.UserRole;
import com.sparta.logistics.user_service.domain.repository.UserRepository;
import com.sparta.logistics.user_service.jwt.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 회원가입
    public AuthSignupResponseDto createUser(AuthSignupRequestDto requestDto, String userIdHeader) {
        Long userId = parseOrDefault(userIdHeader);

        User user = User.builder()
            .username(requestDto.getUsername())
            .password(BCrypt.hashpw(requestDto.getPassword(), BCrypt.gensalt()))
            .slackId(requestDto.getSlackId())
            .role(UserRole.ROLE_USER)
            .build();

        user.createInfo(userId);
        userRepository.save(user);

        return AuthSignupResponseDto.builder()
            .username(user.getUsername())
            .slackId(user.getSlackId())
            .build();
    }

    // 로그인
    public AuthTokenResponseDto loginUser(AuthLoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername()).orElseThrow(() ->
            new EntityNotFoundException("해당하는 사용자가 존재하지 않습니다. 받은 username : " + requestDto.getUsername()));

        boolean passwordMatch = BCrypt.checkpw(requestDto.getPassword(), user.getPassword());
        if (!passwordMatch) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getUsername(), user.getRole().name());

        return AuthTokenResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    // 회원가입시 마스터가 생성하는 것을 가정하여 마스터의 userId를 생성자 필드에 넣을 수 있도록 메서드 제작. master 가 아니라면 기본값 0l 넣기
    private Long parseOrDefault(String header) {
        if (header == null) return 0L;
        try {
            return Long.valueOf(header);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

}
