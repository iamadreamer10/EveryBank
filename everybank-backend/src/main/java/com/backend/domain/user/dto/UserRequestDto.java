package com.backend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class UserRequestDto {

    private String email;

    private String password;

    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
}
