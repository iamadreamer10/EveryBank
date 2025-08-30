package com.backend.domain.contract.service;

import com.backend.domain.account.domain.Account;
import com.backend.domain.account.domain.AccountState;
import com.backend.domain.account.domain.AccountType;
import com.backend.domain.account.repository.AccountRepository;
import com.backend.domain.contract.domain.ContractCondition;
import com.backend.domain.contract.domain.DepositContract;
import com.backend.domain.contract.domain.SavingContract;
import com.backend.domain.contract.dto.*;
import com.backend.domain.contract.repository.DepositContractRepository;
import com.backend.domain.contract.repository.SavingContractRepository;
import com.backend.domain.product.domain.DepositProduct;
import com.backend.domain.product.domain.DepositProductOption;
import com.backend.domain.product.domain.SavingProduct;
import com.backend.domain.product.domain.SavingProductOption;
import com.backend.domain.product.repository.DepositProductOptionRepository;
import com.backend.domain.product.repository.DepositProductRepository;
import com.backend.domain.product.repository.SavingProductOptionRepository;
import com.backend.domain.product.repository.SavingProductRepository;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final DepositContractRepository depositContractRepository;
    private final DepositProductOptionRepository depositProductOptionRepository;
    private final DepositProductRepository depositProductRepository;
    private final SavingProductOptionRepository savingProductOptionRepository;
    private final SavingProductRepository savingProductRepository;
    private final SavingContractRepository savingContractRepository;
    private final AccountRepository accountRepository;

    public DepositSubscriptionResponseDto subscribeDeposit(DepositSubscriptionRequestDto requestDto, SecurityUser securityUser) {
        LocalDate currentDate = LocalDate.now();

        // 1. 검증
        DepositProductOption option = depositProductOptionRepository.findById(requestDto.getOptionId())
                .orElseThrow(() -> new NoSuchElementException("상품에 맞는 옵션을 찾을 수 없습니다: " + requestDto.getOptionId()));

        DepositProduct product = validateAndGetDepositProduct(requestDto.getProductCode());
        LocalDate maturityDate = currentDate.plusMonths(option.getSaveTerm());

        // 2. 계좌 생성
        Account account = createAccount(securityUser.getId(), product.getCompanyCode(), maturityDate, AccountType.DEPOSIT, requestDto.getTotalAmount());

        // 3. 정기예금 계약 생성
        DepositContract depositContract = DepositContract.builder()
                .userId(account.getUserId())
                .depositProduct(product)
                .contractDate(currentDate)
                .maturityDate(maturityDate)
                .depositProductOption(option)
                .payment(account.getCurrentBalance())
                .contractCondition(ContractCondition.IN_PROGRESS)
                .accountId(account.getId())
                .build();
        depositContractRepository.save(depositContract);

        return DepositSubscriptionResponseDto.builder()
                .contractId(depositContract.getContractId())
                .userId(depositContract.getUserId())
                .nickname(securityUser.getNickname())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .totalAmount(depositContract.getPayment())
                .contractDate(depositContract.getContractDate())
                .option(convertToDepositDto(option))
                .companyName(product.getCompanyName())
                .maturityDate(depositContract.getMaturityDate())
                .build();
    }

    public SavingSubscriptionResponseDto subscribeSaving(SavingSubscriptionRequestDto requestDto, SecurityUser securityUser) {
        LocalDate currentDate = LocalDate.now();
        log.info(String.valueOf(requestDto));

        // 1. 검증
        SavingProductOption option = savingProductOptionRepository.findById(requestDto.getOptionId())
                .orElseThrow(() -> new NoSuchElementException("상품에 맞는 옵션을 찾을 수 없습니다: " + requestDto.getOptionId()));

        SavingProduct product = validateAndGetSavingProduct(requestDto.getProductCode());
        LocalDate maturityDate = currentDate.plusMonths(option.getSaveTerm());

        // 2. 계좌 생성
        Account account = createAccount(securityUser.getId(), product.getCompanyCode(), maturityDate, AccountType.SAVING, 0L);

        // 3. 적금 계약 생성
        SavingContract savingContract = SavingContract.builder()
                .userId(account.getUserId())
                .savingProduct(product)
                .contractDate(currentDate)
                .maturityDate(maturityDate)
                .savingProductOption(option)
                .monthlyPayment(requestDto.getMonthlyAmount())
                .currentPaymentCount(0)
                .contractCondition(ContractCondition.IN_PROGRESS)
                .accountId(account.getId())
                .build();
        savingContractRepository.save(savingContract);

        return SavingSubscriptionResponseDto.builder()
                .contractId(savingContract.getContractId())
                .userId(savingContract.getUserId())
                .nickname(securityUser.getNickname())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .companyName(product.getCompanyName())
                .monthlyPayment(savingContract.getMonthlyPayment())
                .currentPaymentCount(savingContract.getCurrentPaymentCount())
                .latestPaymentDate(savingContract.getLatestPaymentDate())
                .contractDate(savingContract.getContractDate())
                .maturityDate(savingContract.getMaturityDate())
                .option(convertToSavingDto(option))
                .build();
    }

    // 공통 계좌 생성 로직
    private Account createAccount(Long userId, String companyCode, LocalDate maturityDate, AccountType accountType, Long payment) {
        Account account = Account.builder()
                .userId(userId)
                .companyCode(companyCode)
                .currentBalance(payment)
                .accountState(AccountState.ACTIVE)
                .lastTransactionDate(LocalDateTime.now())
                .maturityDate(maturityDate)
                .accountType(accountType)
                .build();
        return accountRepository.save(account);
    }

    // 검증 로직 분리
    private DepositProduct validateAndGetDepositProduct(String productCode) {
        DepositProduct product = depositProductRepository.findByProductCode(productCode);
        if (product == null) {
            throw new RuntimeException("상품을 찾을 수 없습니다: " + productCode);
        }
        return product;
    }

    private SavingProduct validateAndGetSavingProduct(String productCode) {
        SavingProduct product = savingProductRepository.findByProductCode(productCode);
        if (product == null) {
            throw new RuntimeException("상품을 찾을 수 없습니다: " + productCode);
        }
        return product;
    }

    // DTO 변환 메서드들
    private DepositProductOptionResponseDto convertToDepositDto(DepositProductOption option) {
        return DepositProductOptionResponseDto.builder()
                .id(option.getId())
                .interestRateType(option.getInterestRateType())
                .interestRateTypeName(option.getInterestRateTypeName())
                .saveTerm(option.getSaveTerm())
                .interestRate(option.getInterestRate())
                .interestRate2(option.getInterestRate2())
                .build();
    }

    private SavingProductOptionResponseDto convertToSavingDto(SavingProductOption option) {
        return SavingProductOptionResponseDto.builder()
                .id(option.getId())
                .interestRateType(option.getInterestRateType())
                .interestRateTypeName(option.getInterestRateTypeName())
                .saveTerm(option.getSaveTerm())
                .reverseType(option.getReverseType())
                .reverseTypeName(option.getReverseTypeName())
                .interestRate(option.getInterestRate())
                .interestRate2(option.getInterestRate2())
                .build();
    }


//     새로 추가되는 메서드들
//    public MaturityCalculationDto calculateMaturity(Integer accountId, SecurityUser securityUser) {
//        // 계좌 조회 및 검증 로직
//        // 예금/적금 구분해서 계산 호출
//    }
//
//    private MaturityCalculationDto calculateDepositMaturity(Account depositAccount, Account checkingAccount) {
//        // 예금 계약 조회 → 이자 계산 → DTO 빌드
//    }
//
//    private MaturityCalculationDto calculateSavingMaturity(Account savingAccount, Account checkingAccount) {
//        // 적금 계약 조회 → 이자 계산 → DTO 빌드
//    }
//
//    private Long calculateInterest(Long principal, BigDecimal yearlyRate, Integer months, char rateType) {
//        // 예금 이자 계산 (단리/복리)
//    }
//
//    private Long calculateSavingInterest(Long monthlyPayment, BigDecimal yearlyRate, Integer paymentCount, char rateType) {
//        // 적금 이자 계산 (매월 납입 고려)
//    }
//
//    private Account findUserCheckingAccount(Long userId) {
//        // 사용자 입출금계좌 찾기 (AccountService에서 복사)
//    }
}