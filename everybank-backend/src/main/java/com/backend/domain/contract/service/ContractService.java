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
import com.backend.domain.transaction.domain.Transaction;
import com.backend.domain.transaction.domain.TransactionType;
import com.backend.domain.transaction.repository.TransactionRepository;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TransactionRepository transactionRepository;

    @Transactional
    public DepositSubscriptionResponseDto subscribeDeposit(DepositSubscriptionRequestDto requestDto, SecurityUser securityUser) {
        log.info("예금 계약 요청 - 사용자: {}, 상품: {}, 금액: {}원",
                securityUser.getId(), requestDto.getProductCode(), requestDto.getTotalAmount());

        LocalDate currentDate = LocalDate.now();

        // 1. 검증
        DepositProductOption option = depositProductOptionRepository.findById(requestDto.getOptionId())
                .orElseThrow(() -> new NoSuchElementException("상품 옵션을 찾을 수 없습니다: " + requestDto.getOptionId()));

        DepositProduct product = validateAndGetDepositProduct(requestDto.getProductCode());
        LocalDate maturityDate = currentDate.plusMonths(option.getSaveTerm());

        // 2. 입출금계좌 조회 및 잔액 확인
        Account checkingAccount = findUserCheckingAccount(securityUser.getId());

        if (checkingAccount.getCurrentBalance() < requestDto.getTotalAmount()) {
            throw new IllegalArgumentException("잔액이 부족합니다. 현재 잔액: " +
                    checkingAccount.getCurrentBalance() + "원, 필요 금액: " + requestDto.getTotalAmount() + "원");
        }

        // 3. 예금계좌 생성 (즉시 전액 입금)
        Account depositAccount = createAccount(securityUser.getId(), product.getCompanyCode(),
                maturityDate, AccountType.DEPOSIT, requestDto.getTotalAmount());

        // 4. 입출금계좌에서 예금계좌로 이체 처리
        transferFromCheckingToDeposit(checkingAccount, depositAccount, requestDto.getTotalAmount());

        // 5. 예금 계약 생성
        DepositContract depositContract = DepositContract.builder()
                .userId(depositAccount.getUserId())
                .depositProduct(product)
                .contractDate(currentDate)
                .maturityDate(maturityDate)
                .depositProductOption(option)
                .payment(requestDto.getTotalAmount()) // 실제 납입 금액
                .contractCondition(ContractCondition.IN_PROGRESS)
                .accountId(depositAccount.getId())
                .build();

        depositContractRepository.save(depositContract);

        log.info("예금 계약 완료 - 계약ID: {}, 계좌ID: {}, 납입액: {}원",
                depositContract.getContractId(), depositAccount.getId(), requestDto.getTotalAmount());

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

    @Transactional
    public SavingSubscriptionResponseDto subscribeSaving(SavingSubscriptionRequestDto requestDto, SecurityUser securityUser) {
        LocalDate currentDate = LocalDate.now();
        log.info("적금 계약 요청 - 사용자: {}, 상품: {}, 월납입액: {}원",
                securityUser.getId(), requestDto.getProductCode(), requestDto.getMonthlyAmount());

        // 1. 검증
        SavingProductOption option = savingProductOptionRepository.findById(requestDto.getOptionId())
                .orElseThrow(() -> new NoSuchElementException("상품에 맞는 옵션을 찾을 수 없습니다: " + requestDto.getOptionId()));

        SavingProduct product = validateAndGetSavingProduct(requestDto.getProductCode());
        LocalDate maturityDate = currentDate.plusMonths(option.getSaveTerm());

        // 2. 적금계좌 생성 (초기 잔액 0원)
        Account savingAccount = createAccount(securityUser.getId(), product.getCompanyCode(),
                maturityDate, AccountType.SAVING, 0L);

        // 3. 적금 계약 생성
        SavingContract savingContract = SavingContract.builder()
                .userId(savingAccount.getUserId())
                .savingProduct(product)
                .contractDate(currentDate)
                .maturityDate(maturityDate)
                .savingProductOption(option)
                .monthlyPayment(requestDto.getMonthlyAmount())
                .currentPaymentCount(0) // 초기 납입 횟수 0
                .contractCondition(ContractCondition.IN_PROGRESS)
                .accountId(savingAccount.getId())
                .build();

        savingContractRepository.save(savingContract);

        log.info("적금 계약 완료 - 계약ID: {}, 계좌ID: {}, 월납입액: {}원",
                savingContract.getContractId(), savingAccount.getId(), requestDto.getMonthlyAmount());

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

    // 입출금계좌에서 예금계좌로 이체 처리
    @Transactional
    public void transferFromCheckingToDeposit(Account checkingAccount, Account depositAccount, Long amount) {
        log.info("예금 납입 이체 - 입출금계좌: {} → 예금계좌: {}, 금액: {}원",
                checkingAccount.getId(), depositAccount.getId(), amount);

        // 입출금계좌 잔액 차감
        checkingAccount.setCurrentBalance(checkingAccount.getCurrentBalance() - amount);
        checkingAccount.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(checkingAccount);

        // 예금계좌 잔액은 이미 생성 시 설정됨
        depositAccount.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(depositAccount);

        // 거래내역 생성 (예금 납입)
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PAYMENT) // 예금 납입
                .amount(amount)
                .fromAccountId(checkingAccount.getId())
                .toAccountId(depositAccount.getId())
                .currentBalance(depositAccount.getCurrentBalance()) // 예금계좌 잔액
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        log.info("예금 납입 완료 - 거래ID: {}, 입출금계좌 잔액: {}원, 예금계좌 잔액: {}원",
                transaction.getTransactionId(), checkingAccount.getCurrentBalance(), depositAccount.getCurrentBalance());
    }

    // 공통 계좌 생성 로직
    private Account createAccount(Long userId, String companyCode, LocalDate maturityDate, AccountType accountType, Long initialBalance) {
        Account account = Account.builder()
                .userId(userId)
                .companyCode(companyCode)
                .currentBalance(initialBalance)
                .accountState(AccountState.ACTIVE)
                .lastTransactionDate(LocalDateTime.now())
                .maturityDate(maturityDate)
                .accountType(accountType)
                .build();
        return accountRepository.save(account);
    }

    // 사용자의 입출금계좌 찾기
    private Account findUserCheckingAccount(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElse(List.of())
                .stream()
                .filter(account -> account.getAccountType() == AccountType.CHECK &&
                        account.getAccountState() == AccountState.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("입출금계좌가 없습니다. 먼저 입출금계좌를 개설해주세요."));
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

        Long principal = contract.getPayment();
        LocalDate contractDate = contract.getContractDate();
        LocalDate maturityDate = contract.getMaturityDate();
        LocalDate currentDate = LocalDate.now();
        BigDecimal contractInterestRate = contract.getDepositProductOption().getInterestRate2(); // 약정이율
        char rateType = contract.getDepositProductOption().getInterestRateType();

        // 실제 운용기간 및 만기 여부 확인
        LocalDate endDate = currentDate.isBefore(maturityDate) ? currentDate : maturityDate;
        long actualDays = contractDate.until(endDate).getDays();
        boolean isMatured = currentDate.isAfter(maturityDate) || currentDate.isEqual(maturityDate);

        BigDecimal appliedInterestRate;
        Long interest;

        if (isMatured) {
            // 🎉 만기 시: 약정이율 100% 보장
            appliedInterestRate = contractInterestRate;
            long totalContractDays = contractDate.until(maturityDate).getDays();
            interest = calculateDepositInterestByDays(principal, appliedInterestRate, totalContractDays, rateType);

            log.info("✅ 만기해지 - 약정이율 {}% 전액 적용, 만기일수: {}일", contractInterestRate, totalContractDays);

        } else {
            // 🚨 중도해지 시: 변경 후 규칙 적용 (약정이율 × 50% + 최저이율 보장)
            appliedInterestRate = calculateEarlyTerminationRate(contractInterestRate, actualDays);
            interest = calculateDepositInterestByDays(principal, appliedInterestRate, actualDays, rateType);

            log.warn("⚠️ 중도해지 - 약정이율: {}%, 적용이율: {}%, 보유일수: {}일",
                    contractInterestRate, appliedInterestRate, actualDays);
        }

        Long totalPayout = principal + interest;


        log.info("예금 만기정산 완료 - 원금: {}원, 이자: {}원, 총액: {}원, 만기여부: {}",
                principal, interest, totalPayout, isMatured);

        return MaturityCalculationDto.builder()
                .accountId(depositAccount.getId())
                .accountType(depositAccount.getAccountType())
                .productName(contract.getDepositProduct().getProductName())
                .companyName(contract.getDepositProduct().getCompanyName())
                .contractDate(contract.getContractDate())
                .maturityDate(contract.getMaturityDate())
                .saveTerm(contract.getDepositProductOption().getSaveTerm())
                .interestRate(contract.getDepositProductOption().getInterestRate())
                .interestRate2(contract.getDepositProductOption().getInterestRate2())
                .interestRateTypeName(contract.getDepositProductOption().getInterestRateTypeName())
                .totalPrincipal(principal)
                .totalInterest(interest)
                .totalPayout(totalPayout)
                .currentCheckAmount(checkingAccount.getCurrentBalance())
                .isMatured(isMatured)
                .build();
    }

    // 중도해지 시 이자율 계산 (변경 후 규칙)
    private BigDecimal calculateEarlyTerminationRate(BigDecimal contractRate, long holdingDays) {
        // 기본 중도해지 이자율: 약정이율 × 50%
        BigDecimal baseEarlyRate = contractRate.multiply(BigDecimal.valueOf(0.5));

        // 보유기간별 최저이율 보장
        BigDecimal minimumRate = getMinimumRateByHoldingPeriod(holdingDays);

        // 둘 중 높은 이자율 적용
        BigDecimal appliedRate = baseEarlyRate.compareTo(minimumRate) >= 0 ? baseEarlyRate : minimumRate;

        log.debug("중도해지 이자율 계산 - 약정: {}%, 기본중도: {}%, 최저보장: {}%, 최종적용: {}%",
                contractRate, baseEarlyRate, minimumRate, appliedRate);

        return appliedRate;
    }

    // 🔧 일 단위 예금 이자 계산 메서드 (누락된 메서드)
    private Long calculateDepositInterestByDays(Long principal, BigDecimal yearlyRate, long days, char rateType) {
        double rate = yearlyRate.doubleValue() / 100.0; // 연이율
        double dailyRate = rate / 365.0; // 일이율

        log.debug("예금 이자 계산 - 원금: {}원, 연이율: {}%, 운용일수: {}일, 유형: {}",
                principal, yearlyRate, days, rateType == 'S' ? "단리" : "복리");

        if (rateType == 'S') { // 단리
            long interest = Math.round(principal * dailyRate * days);
            log.debug("단리 계산 결과: {}원", interest);
            return interest;
        } else { // 복리 (일복리)
            double compound = Math.pow(1 + dailyRate, days);
            long interest = Math.round(principal * (compound - 1));
            log.debug("복리 계산 결과: {}원 (복리계수: {})", interest, compound);
            return interest;
        }
    }

    private BigDecimal getMinimumRateByHoldingPeriod(long holdingDays) {
        if (holdingDays < 30) { // 1개월 미만
            return BigDecimal.valueOf(0.1);
        } else if (holdingDays < 90) { // 1개월 이상 ~ 3개월 미만
            return BigDecimal.valueOf(0.3);
        } else { // 3개월 이상
            return BigDecimal.valueOf(0.5);
        }
    }

    // 3. 적금 만기정산 계산
    private MaturityCalculationDto calculateSavingMaturity(Account savingAccount, Account checkingAccount) {
        log.info("적금 만기정산 계산 시작 - 계좌ID: {}", savingAccount.getId());

        SavingContract contract = savingContractRepository.findByAccountId(savingAccount.getId())
                .orElseThrow(() -> new NoSuchElementException("적금 계약을 찾을 수 없습니다: " + savingAccount.getId()));

        Long monthlyPayment = contract.getMonthlyPayment();
        Integer currentPaymentCount = contract.getCurrentPaymentCount();
        LocalDate contractDate = contract.getContractDate();
        LocalDate maturityDate = contract.getMaturityDate();
        LocalDate currentDate = LocalDate.now();
        BigDecimal contractInterestRate = contract.getSavingProductOption().getInterestRate2();
        char rateType = contract.getSavingProductOption().getInterestRateType();

        // 원금 계산
        Long principal = monthlyPayment * currentPaymentCount;
        boolean isMatured = currentDate.isAfter(maturityDate) || currentDate.isEqual(maturityDate);

        Long interest;

        if (isMatured) {
            // 🎉 만기 시: 약정이율 100% 적용
            interest = calculateSavingInterestByActualPayments(
                    monthlyPayment, contractInterestRate, rateType,
                    contractDate, maturityDate, maturityDate, currentPaymentCount);

            log.info("✅ 적금 만기해지 - 약정이율 {}% 전액 적용", contractInterestRate);

        } else {
            // 🚨 중도해지 시: 각 납입회차별로 중도해지 이자율 적용
            long holdingDays = contractDate.until(currentDate).getDays();
            BigDecimal appliedRate = calculateEarlyTerminationRate(contractInterestRate, holdingDays);

            interest = calculateSavingInterestByActualPayments(
                    monthlyPayment, appliedRate, rateType,
                    contractDate, currentDate, maturityDate, currentPaymentCount);

            log.warn("⚠️ 적금 중도해지 - 약정이율: {}%, 적용이율: {}%, 보유일수: {}일",
                    contractInterestRate, appliedRate, holdingDays);
        }

        Long totalPayout = principal + interest;

        log.info("적금 정산 완료 - 원금: {}원({}회), 이자: {}원, 총액: {}원, 해지유형: {}",
                principal, currentPaymentCount, interest, totalPayout, isMatured ? "만기해지" : "중도해지");

        return MaturityCalculationDto.builder()
                .accountId(savingAccount.getId())
                .accountType(savingAccount.getAccountType())
                .productName(contract.getSavingProduct().getProductName())
                .companyName(contract.getSavingProduct().getCompanyName())
                .contractDate(contractDate)
                .maturityDate(maturityDate)
                .saveTerm(contract.getSavingProductOption().getSaveTerm())
                .interestRate(contract.getSavingProductOption().getInterestRate())
                .interestRate2(isMatured ? contractInterestRate : calculateEarlyTerminationRate(contractInterestRate, contractDate.until(currentDate).getDays()))
                .interestRateTypeName(contract.getSavingProductOption().getInterestRateTypeName())
                .monthlyPayment(monthlyPayment)
                .totalPaymentCount(contract.getSavingProductOption().getSaveTerm())
                .currentPaymentCount(currentPaymentCount)
                .totalPrincipal(principal)
                .totalInterest(interest)
                .totalPayout(totalPayout)
                .currentCheckAmount(checkingAccount.getCurrentBalance())
                .isMatured(isMatured)
                .build();
    }

    private Long calculateSavingInterestByActualPayments(Long monthlyPayment, BigDecimal yearlyRate,
                                                         char rateType, LocalDate contractDate, LocalDate currentDate, LocalDate maturityDate,
                                                         Integer currentPaymentCount) {

        double rate = yearlyRate.doubleValue() / 100.0;
        double dailyRate = rate / 365.0;
        long totalInterest = 0;

        log.debug("적금 이자 계산 시작 - 월납입액: {}원, 연이율: {}%, 납입횟수: {}회",
                monthlyPayment, yearlyRate, currentPaymentCount);

        // 각 납입회차별로 이자 계산
        for (int i = 1; i <= currentPaymentCount; i++) {
            // 각 납입회차의 실제 납입일 계산 (계약일 + i개월)
            LocalDate paymentDate = contractDate.plusMonths(i);

            // 해당 납입액의 실제 운용기간 계산
            LocalDate endDate = currentDate.isBefore(maturityDate) ? currentDate : maturityDate;
            long daysFromPayment = paymentDate.until(endDate).getDays();

            if (daysFromPayment <= 0) continue; // 아직 운용기간이 없는 경우

            long monthlyInterest;
            if (rateType == 'S') { // 단리
                monthlyInterest = Math.round(monthlyPayment * dailyRate * daysFromPayment);
            } else { // 복리
                double compound = Math.pow(1 + dailyRate, daysFromPayment);
                monthlyInterest = Math.round(monthlyPayment * (compound - 1));
            }

            totalInterest += monthlyInterest;

            log.debug("{}회차 납입 - 납입일: {}, 운용일수: {}일, 이자: {}원",
                    i, paymentDate, daysFromPayment, monthlyInterest);
        }

        log.debug("적금 총 이자 계산 결과: {}원", totalInterest);
        return totalInterest;
    }
}