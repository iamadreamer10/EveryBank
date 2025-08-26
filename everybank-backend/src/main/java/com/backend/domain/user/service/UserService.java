package com.backend.domain.user.service;

import com.backend.domain.user.domain.User;
import com.backend.domain.user.dto.UserRequestDto;
import com.backend.domain.user.dto.UserResponseDto;
import com.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto join(UserRequestDto requestDto) {
        boolean emailChecked = userRepository.existsByEmail(requestDto.getEmail());

        // 이메일이 이미 존재하면 에러 던지기
        if (emailChecked) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        log.info(encodedPassword);
        requestDto.setPassword("");

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .birthdate(requestDto.getBirthdate())
                .nickname(requestDto.getNickname())
                .build();

        user = userRepository.save(user);
        return new UserResponseDto(user);
    }
}
