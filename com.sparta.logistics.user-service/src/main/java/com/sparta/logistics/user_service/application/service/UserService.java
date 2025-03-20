package com.sparta.logistics.user_service.application.service;

import com.sparta.logistics.user_service.application.dto.request.UserPasswordUpdateRequestDto;
import com.sparta.logistics.user_service.application.dto.request.UserRoleUpdateRequestDto;
import com.sparta.logistics.user_service.application.dto.request.UserUpdateRequestDto;
import com.sparta.logistics.user_service.application.dto.response.UserRoleUpdateResponseDto;
import com.sparta.logistics.user_service.application.dto.response.UserSearchMeResponseDto;
import com.sparta.logistics.user_service.application.dto.response.UserSearchResponseDto;
import com.sparta.logistics.user_service.application.dto.response.UserUpdateResponseDto;
import com.sparta.logistics.user_service.domain.entity.User;
import com.sparta.logistics.user_service.domain.entity.UserRole;
import com.sparta.logistics.user_service.domain.repository.UserRepository;
import com.sparta.logistics.user_service.presentation.feignClient.DeliveryManagerFeignClient;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeliveryManagerFeignClient deliveryManagerFeignClient;

    // 유저 검색 : 본인
    public UserSearchMeResponseDto searchMeUser(String userIdHeader) {
        Long userId = Long.parseLong(userIdHeader);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("본인 정보를 찾을 수 없습니다. 헤더로 받은 userId : " + userIdHeader));
        return UserSearchMeResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .slackId(user.getSlackId())
            .role(user.getRole().toString())
            .build();
    }

    // 유저 검색 : userId, username
    public UserSearchResponseDto searchUser(Long userId, String username) {

        User user;
        if (userId != null) {
            user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "user_id 에 해당하는 유저가 없습니다. 받은 user_id : " + userId));

        } else {
            user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                    "username 에 해당하는 유저가 없습니다. 받은 username : " + username));

        }
        return UserSearchResponseDto.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .slackId(user.getSlackId())
            .role(user.getRole().toString())
            .build();
    }

    // 유저 수정 : 본인 비밀번호 수정
    @Transactional
    public void updatePassword(String userIdHeader, UserPasswordUpdateRequestDto requestDto) {
        Long userId = Long.parseLong(userIdHeader);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다. userId : " + userId));

        if (BCrypt.checkpw(requestDto.getOldPassword(), user.getPassword())) {
            user.updatePassword(BCrypt.hashpw(requestDto.getNewPassword(), BCrypt.gensalt()));
            user.updateInfo(userId);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

    }

    // MASTER : 유저 프로필 수정
    @Transactional
    public UserUpdateResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto, String userIdHeader) {

        Long updaterId = Long.parseLong(userIdHeader);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("해당하는 유저를 찾을 수 없습니다. 받은 userId : " + userId));

        user.updateUser(requestDto);
        user.updateInfo(updaterId);
        userRepository.save(user);

        return UserUpdateResponseDto.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .slackId(user.getSlackId())
            .role(user.getRole().toString())
            .build();
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userIdHeader, Long userId) {
        Long deleterId = Long.parseLong(userIdHeader);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("삭제 대상 유저를 찾을 수 없습니다. : " + userId));

        user.deleteInfo(deleterId);
        userRepository.save(user);
    }

    // 유저 권한 수정
    @Transactional
    public UserRoleUpdateResponseDto roleUpdateUser(Long targetUserId, String userIdHeader, UserRoleUpdateRequestDto requestDto) {
        Long updaterId = Long.parseLong(userIdHeader);

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new EntityNotFoundException("권한 수정 대상 유저를 찾을 수 없습니다. : " + targetUserId));

        user.updateInfo(updaterId);
        user.updateRole(requestDto);
        userRepository.save(user);

        if (user.getRole() == UserRole.ROLE_DELIVERY_MANAGER) {
            deliveryManagerFeignClient.createDeliveryManager(user.getId(), user.getSlackId());
        }

        return UserRoleUpdateResponseDto.builder()
            .userId(user.getId())
            .slackId(user.getSlackId())
            .newRole(user.getRole().toString())
            .build();

    }
}
