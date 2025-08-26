package com.backend.domain.user.dto;

import com.backend.domain.user.domain.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserResponseDto {

    private Long id;

    private String email;

    private String nickname;

    private LocalDate birthdate;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.birthdate = user.getBirthdate();
    }
}
