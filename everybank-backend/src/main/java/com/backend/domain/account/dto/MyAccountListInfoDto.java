package com.backend.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MyAccountListInfoDto {

    private int count;
    private List<AccountInfoDto>  accountList;


}
