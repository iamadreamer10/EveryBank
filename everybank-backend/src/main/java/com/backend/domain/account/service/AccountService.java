package com.backend.domain.account.service;


import com.backend.domain.account.domain.Account;
import com.backend.domain.account.dto.AccountInfoDto;
import com.backend.domain.account.dto.MyAccountListInfoDto;
import com.backend.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public MyAccountListInfoDto getMyAccounts(Long id) {
        Optional<List<Account>> myAccounts = accountRepository.findByUserId(id);

        if (myAccounts.isEmpty()) {
            return MyAccountListInfoDto.builder()
                    .count(0)
                    .build();
        }

        List<AccountInfoDto> accountInfoDtoList = new ArrayList<>();
        for (Account account : myAccounts.get()) {
            accountInfoDtoList.add(AccountInfoDto.builder()
                    .id(account.getId())
                    .userId(account.getUserId())
                    .accountType(account.getAccountType())
                    .currentBalance(account.getCurrentBalance())
                    .lastTransactionDate(account.getLastTransactionDate())
                    .companyCode(account.getCompanyCode())
                    .maturityDate(account.getMaturityDate())
                    .accountState(account.getAccountState())
                    .build());
        }
        return new MyAccountListInfoDto(accountInfoDtoList.size(), accountInfoDtoList);
    }
}
