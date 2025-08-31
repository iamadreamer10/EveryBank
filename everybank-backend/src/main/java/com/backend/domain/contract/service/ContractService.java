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
import java.util.List;
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


    // 만기정산 메인 메서드
    public MaturityCalculationDto calculateMaturity(Integer accountId, SecurityUser securityUser) {
        log.info("만기정산 확인 요청 - 사용자: {}, 계좌ID: {}", securityUser.getId(), accountId);

        // 1. 상품계좌 조회 및 검증
        Account productAccount = accountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다: " + accountId));

        // 2. 본인 계좌 검증
        if (!productAccount.getUserId().equals(securityUser.getId())) {
            throw new IllegalArgumentException("본인의 계좌만 조회 가능합니다.");
        }

        // 3. 상품계좌 타입 검증
        if (productAccount.getAccountType() == AccountType.CHECK) {
            throw new IllegalArgumentException("입출금계좌는 만기정산 대상이 아닙니다.");
        }

        // 4. 입출금계좌 잔액 조회
        Account checkingAccount = findUserCheckingAccount(securityUser.getId());

        // 5. 계좌 타입에 따른 계약 정보 조회 및 이자 계산
        if (productAccount.getAccountType() == AccountType.DEPOSIT) {
            return calculateDepositMaturity(productAccount, checkingAccount);
        } else if (productAccount.getAccountType() == AccountType.SAVING) {
            return calculateSavingMaturity(productAccount, checkingAccount);
        }

        throw new IllegalArgumentException("지원하지 않는 계좌 타입입니다: " + productAccount.getAccountType());
    }

    // 2. 예금 만기정산 계산
    private MaturityCalculationDto calculateDepositMaturity(Account depositAccount, Account checkingAccount) {
        log.info("예금 만기정산 계산 시작 - 계좌ID: {}", depositAccount.getId());

        // 예금 계약 정보 조회
        DepositContract contract = depositContractRepository.findByAccountId(depositAccount.getId())
                .orElseThrow(() -> new NoSuchElementException("예금 계약을 찾을 수 없습니다: " + depositAccount.getId()));

        log.info("예금 계약 정보 - 계약ID: {}, 원금: {}원, 기간: {}개월, 금리: {}%",
                contract.getContractId(), contract.getPayment(),
                contract.getDepositProductOption().getSaveTerm(),
                contract.getDepositProductOption().getInterestRate2());

        // 기본 정보 추출
        Long principal = contract.getPayment(); // 원금
        Integer termMonths = contract.getDepositProductOption().getSaveTerm(); // 기간(개월)
        BigDecimal interestRate = contract.getDepositProductOption().getInterestRate2(); // 우대금리
        char rateType = contract.getDepositProductOption().getInterestRateType(); // 금리유형

        // 이자 계산
        Long interest = calculateDepositInterest(principal, interestRate, termMonths, rateType);
        Long totalPayout = principal + interest;

        // 만기 도래 여부 확인
        boolean isMatured = LocalDate.now().isAfter(depositAccount.getMaturityDate()) ||
                LocalDate.now().isEqual(depositAccount.getMaturityDate());

        log.info("예금 만기정산 완료 - 원금: {}원, 이자: {}원, 총액: {}원, 만기여부: {}",
                principal, interest, totalPayout, isMatured);

        return MaturityCalculationDto.builder()
                .accountId(depositAccount.getId())
                .accountType(depositAccount.getAccountType())
                .productName(contract.getDepositProduct().getProductName())
                .companyName(contract.getDepositProduct().getCompanyName())
                .contractDate(contract.getContractDate())
                .maturityDate(contract.getMaturityDate())
                .saveTerm(termMonths)
                .interestRate(contract.getDepositProductOption().getInterestRate())
                .interestRate2(contract.getDepositProductOption().getInterestRate2())
                .interestRateTypeName(contract.getDepositProductOption().getInterestRateTypeName())
                .principal(principal)
                .interest(interest)
                .totalPayout(totalPayout)
                .currentCheckAmount(checkingAccount.getCurrentBalance())
                .isMatured(isMatured)
                .build();
    }

    // 3. 적금 만기정산 계산
    private MaturityCalculationDto calculateSavingMaturity(Account savingAccount, Account checkingAccount) {
        log.info("적금 만기정산 계산 시작 - 계좌ID: {}", savingAccount.getId());

        // 적금 계약 정보 조회
        SavingContract contract = savingContractRepository.findByAccountId(savingAccount.getId())
                .orElseThrow(() -> new NoSuchElementException("적금 계약을 찾을 수 없습니다: " + savingAccount.getId()));

        log.info("적금 계약 정보 - 계약ID: {}, 월납입액: {}원, 납입횟수: {}/{}회, 금리: {}%",
                contract.getContractId(), contract.getMonthlyPayment(),
                contract.getCurrentPaymentCount(), contract.getSavingProductOption().getSaveTerm(),
                contract.getSavingProductOption().getInterestRate2());

        // 기본 정보 추출
        Long monthlyPayment = contract.getMonthlyPayment(); // 월 납입액
        Integer currentPaymentCount = contract.getCurrentPaymentCount(); // 현재 납입 횟수
        Integer totalPaymentCount = contract.getSavingProductOption().getSaveTerm(); // 총 납입 횟수
        BigDecimal interestRate = contract.getSavingProductOption().getInterestRate2(); // 우대금리
        char rateType = contract.getSavingProductOption().getInterestRateType(); // 금리유형

        // 원금 계산 (현재까지 납입한 금액)
        Long principal = monthlyPayment * currentPaymentCount;

        // 적금 이자 계산 (매월 납입 기준)
        Long interest = calculateSavingInterest(monthlyPayment, interestRate, currentPaymentCount, rateType);
        Long totalPayout = principal + interest;

        // 만기 도래 여부 확인
        boolean isMatured = LocalDate.now().isAfter(savingAccount.getMaturityDate()) ||
                LocalDate.now().isEqual(savingAccount.getMaturityDate());

        log.info("적금 만기정산 완료 - 원금: {}원({}회×{}원), 이자: {}원, 총액: {}원, 만기여부: {}",
                principal, currentPaymentCount, monthlyPayment, interest, totalPayout, isMatured);

        return MaturityCalculationDto.builder()
                .accountId(savingAccount.getId())
                .accountType(savingAccount.getAccountType())
                .productName(contract.getSavingProduct().getProductName())
                .companyName(contract.getSavingProduct().getCompanyName())
                .contractDate(contract.getContractDate())
                .maturityDate(contract.getMaturityDate())
                .saveTerm(totalPaymentCount)
                .interestRate(contract.getSavingProductOption().getInterestRate())
                .interestRate2(contract.getSavingProductOption().getInterestRate2())
                .interestRateTypeName(contract.getSavingProductOption().getInterestRateTypeName())
                .monthlyPayment(monthlyPayment)
                .totalPaymentCount(totalPaymentCount)
                .currentPaymentCount(currentPaymentCount)
                .principal(principal)
                .interest(interest)
                .totalPayout(totalPayout)
                .currentCheckAmount(checkingAccount.getCurrentBalance())
                .isMatured(isMatured)
                .build();
    }

    // 🧮 4. 예금 이자 계산 (단리/복리)
    private Long calculateDepositInterest(Long principal, BigDecimal yearlyRate, Integer months, char rateType) {
        double rate = yearlyRate.doubleValue() / 100.0; // 퍼센트를 소수로 변환
        double monthlyRate = rate / 12.0; // 월 이율

        log.debug("예금 이자 계산 - 원금: {}원, 연이율: {}%, 기간: {}개월, 유형: {}",
                principal, yearlyRate, months, rateType == 'S' ? "단리" : "복리");

        if (rateType == 'S') { // 단리 (Simple Interest)
            long interest = Math.round(principal * monthlyRate * months);
            log.debug("단리 계산 결과: {}원", interest);
            return interest;
        } else { // 복리 (Compound Interest)
            double compound = Math.pow(1 + monthlyRate, months);
            long interest = Math.round(principal * (compound - 1));
            log.debug("복리 계산 결과: {}원 (복리계수: {})", interest, compound);
            return interest;
        }
    }

    // 🧮 5. 적금 이자 계산 (매월 납입 고려)
    private Long calculateSavingInterest(Long monthlyPayment, BigDecimal yearlyRate, Integer paymentCount, char rateType) {
        double rate = yearlyRate.doubleValue() / 100.0; // 퍼센트를 소수로 변환
        double monthlyRate = rate / 12.0; // 월 이율

        log.debug("적금 이자 계산 - 월납입액: {}원, 연이율: {}%, 납입횟수: {}회, 유형: {}",
                monthlyPayment, yearlyRate, paymentCount, rateType == 'S' ? "단리" : "복리");

        long totalInterest = 0;

        // 각 납입월별로 이자 계산
        for (int i = 1; i <= paymentCount; i++) {
            int remainingMonths = paymentCount - i + 1; // 해당 납입액이 이자를 받을 개월수

            if (rateType == 'S') { // 단리
                long monthlyInterest = Math.round(monthlyPayment * monthlyRate * remainingMonths);
                totalInterest += monthlyInterest;
                log.debug("{}회차 납입 단리: {}원 ({}개월)", i, monthlyInterest, remainingMonths);
            } else { // 복리
                double compound = Math.pow(1 + monthlyRate, remainingMonths);
                long monthlyInterest = Math.round(monthlyPayment * (compound - 1));
                totalInterest += monthlyInterest;
                log.debug("{}회차 납입 복리: {}원 ({}개월, 복리계수: {})", i, monthlyInterest, remainingMonths, compound);
            }
        }

        log.debug("적금 총 이자 계산 결과: {}원", totalInterest);
        return totalInterest;
    }

    // 🔍 6. 사용자의 입출금계좌 찾기
    private Account findUserCheckingAccount(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElse(List.of())
                .stream()
                .filter(account -> account.getAccountType() == AccountType.CHECK &&
                        account.getAccountState() == AccountState.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("입출금계좌가 없습니다. 먼저 입출금계좌를 개설해주세요."));
    }
}