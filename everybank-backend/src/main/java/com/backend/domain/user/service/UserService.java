package com.backend.domain.user.service;

import com.backend.domain.user.domain.User;
import com.backend.domain.user.dto.LoginDto;
import com.backend.domain.user.dto.PasswordChangeDto;
import com.backend.domain.user.dto.UserRequestDto;
import com.backend.domain.user.dto.UserResponseDto;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.exception.exceptions.InvalidPasswordException;
import com.backend.global.exception.exceptions.UserNotFoundException;
import com.backend.global.security.SecurityUser;
import com.backend.global.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    public UserResponseDto join(UserRequestDto requestDto) {
        boolean emailChecked = checkEmail(requestDto.getEmail());

        // 이메일이 이미 존재하면 에러 던지기
        if (emailChecked) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
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

    public Map<String, String> login(LoginDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword(),
                        Collections.singleton(new SimpleGrantedAuthority("AUTHORITY")));

        Map<String, String> tokenMap = jwtTokenProvider.generateToken(user.getId(), user.getNickname(), authentication);

        tokenMap.put("id", Long.toString(user.getId()));
        tokenMap.put("email", user.getEmail());
        tokenMap.put("nickname", user.getNickname());

        // redis에 저장
        redisTemplate.opsForValue().set(
                "refresh:" + user.getEmail(),
                tokenMap.get("refresh"),
                jwtTokenProvider.getREFRESH_TOKEN_EXPIRE_TIME(),
                TimeUnit.MICROSECONDS
        );

        return tokenMap;

    }

    @Transactional
    public void logout(String accessToken, SecurityUser securityUser) {
        redisTemplate.delete("refresh:"+securityUser.getEmail());
        redisTemplate.opsForValue().set("blacklist:"+accessToken, "logout", jwtTokenProvider.getACCESS_TOKEN_EXPIRE_TIME(), TimeUnit.MILLISECONDS);
    }

    public boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void passwordCorrectionCheck(PasswordChangeDto passwords, SecurityUser securityUser) {
        if (!passwordEncoder.matches(passwords.getOldPassword(), securityUser.getPassword())) {
            throw new InvalidPasswordException();
        }
    }

    public void setPassword(String password, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        user.setPassword(passwordEncoder.encode(password));
    }

}
