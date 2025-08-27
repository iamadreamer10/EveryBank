package com.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;
}
