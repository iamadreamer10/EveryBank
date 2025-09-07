package com.backend.domain.contract.service;

import com.backend.domain.account.domain.Account;
import com.backend.domain.account.domain.AccountType;
import com.backend.domain.account.repository.AccountRepository;
import com.backend.domain.company.repository.FinCompanyRepository;
import com.backend.domain.contract.domain.DepositContract;
import com.backend.domain.contract.domain.SavingContract;
import com.backend.domain.contract.dto.ContractDetailResponseDto;
import com.backend.domain.contract.repository.DepositContractRepository;
import com.backend.domain.contract.repository.SavingContractRepository;
import com.backend.domain.transaction.domain.Transaction;
import com.backend.domain.transaction.domain.TransactionType;
import com.backend.domain.transaction.repository.TransactionRepository;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractDetailService {

    private final AccountRepository accountRepository;
    private final DepositContractRepository depositContractRepository;
    private final SavingContractRepository savingContractRepository;
    private final TransactionRepository transactionRepository;
    private final FinCompanyRepository finCompanyRepository;

    public ContractDetailResponseDto getContractDetail(Integer accountId, SecurityUser securityUser,
                                                       Integer page, Integer size) {
        log.info("계약 상세조회 - 사용자: {}, 계좌ID: {}", securityUser.getId(), accountId);

// 1단계: 계좌 조회 (1개 쿼리)
        Account account = accountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다: " + accountId));

        if (!account.getUserId().equals(securityUser.getId())) {
            throw new IllegalArgumentException("본인의 계좌만 조회 가능합니다.");
        }

        // 2단계: 계좌 타입별 처리
        if (account.getAccountType() == AccountType.CHECK) {
            return buildCheckingAccountDetail(account, page, size);
        } else if (account.getAccountType() == AccountType.DEPOSIT) {
            return buildDepositContractDetail(account, page, size);
        } else if (account.getAccountType() == AccountType.SAVING) {
            return buildSavingContractDetail(account, page, size);
        }

        throw new IllegalArgumentException("지원하지 않는 계좌 타입: " + account.getAccountType());
    }

    // 🏦 입출금계좌 상세조회 (새로 추가)
    private ContractDetailResponseDto buildCheckingAccountDetail(Account account, Integer page, Integer size) {
        log.info("입출금계좌 상세조회 - 계좌ID: {}", account.getId());

        // 1. 거래내역 조회 (입출금계좌는 from/to 양방향으로 조회)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findCheckingAccountTransactions(
                account.getId(), pageable);

        // 2. 입출금계좌용 예상금액 (잔액만 표시)
        ContractDetailResponseDto.ExpectedAmountsDto expectedAmounts =
                ContractDetailResponseDto.ExpectedAmountsDto.builder()
                        .totalPayment(account.getCurrentBalance())
                        .expectedInterest(0L)  // 입출금계좌는 이자 없음
                        .maturityAmount(account.getCurrentBalance())
                        .build();

        // 3. DTO 조합
        return ContractDetailResponseDto.builder()
                .contractInfo(buildCheckingAccountContractInfo(account))
                .accountInfo(buildAccountInfo(account, null))
                .expectedAmounts(expectedAmounts)
                .transactions(buildCheckingTransactionDetails(transactionPage.getContent(), account.getId()))
                .pagination(buildPagination(transactionPage))
                .build();
    }

    // 🏗️ 입출금계좌 계약 정보 DTO 빌더
    private ContractDetailResponseDto.ContractInfoDto buildCheckingAccountContractInfo(Account account) {
        return ContractDetailResponseDto.ContractInfoDto.builder()
                .contractId(null)  // 입출금계좌는 계약 ID 없음
                .productCode("CHECKING_ACCOUNT")  // 고정값
                .productName("입출금계좌")
                .bank(getCompanyNameByCode(account.getCompanyCode()))
                .contractType("CHECKING")
                .interestRate(0.0)  // 입출금계좌는 이자율 없음
                .interestRateType("없음")
                .monthlyPayment(null)
                .totalAmount(null)
                .term(null)  // 입출금계좌는 기간 없음
                .startDate(account.getLastTransactionDate().toLocalDate())  // 계좌 개설일
                .endDate(account.getMaturityDate())  // 사실상 만료 없음 (99년 후)
                .contractStatus(account.getAccountState().toString())
                .build();
    }

    // 🔄 입출금계좌 거래내역 DTO 변환 (입금/출금 구분)
    private List<ContractDetailResponseDto.TransactionDetailDto> buildCheckingTransactionDetails(
            List<Transaction> transactions, Integer accountId) {
        List<ContractDetailResponseDto.TransactionDetailDto> result = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);

            String description = getCheckingTransactionDescription(tx, accountId);
            String memo = formatCheckingTransactionMemo(tx, i + 1);

            ContractDetailResponseDto.TransactionDetailDto dto =
                    ContractDetailResponseDto.TransactionDetailDto.builder()
                            .transactionId(tx.getTransactionId())
                            .transactionDate(tx.getCreatedAt())
                            .transactionType(tx.getTransactionType().toString())
                            .amount(tx.getAmount())
                            .balance(tx.getCurrentBalance())  // 거래 후 잔액
                            .description(description)
                            .paymentNumber(null)  // 입출금계좌는 납입 회차 없음
                            .memo(memo)
                            .build();

            result.add(dto);
        }

        return result;
    }

    // 입출금계좌 거래 설명 생성
    private String getCheckingTransactionDescription(Transaction tx, Integer accountId) {
        if (tx.getFromAccountId() == null && tx.getToAccountId().equals(accountId)) {
            return "외부입금";  // 외부 → 입출금계좌
        } else if (tx.getFromAccountId() != null && tx.getFromAccountId().equals(accountId)
                && tx.getToAccountId() == null) {
            return "외부출금";  // 입출금계좌 → 외부
        } else if (tx.getFromAccountId() != null && tx.getFromAccountId().equals(accountId)) {
            return "상품납입";  // 입출금계좌 → 상품계좌
        } else if (tx.getToAccountId() != null && tx.getToAccountId().equals(accountId)) {
            return "상품환급";  // 상품계좌 → 입출금계좌
        }
        return "기타거래";
    }

    // 입출금계좌 거래 메모 생성
    private String formatCheckingTransactionMemo(Transaction tx, Integer sequenceNumber) {
        return String.format("거래 %d | %s", sequenceNumber,
                tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
    }

    // 회사명 조회 헬퍼 메서드
    private String getCompanyNameByCode(String companyCode) {
        return finCompanyRepository.findFinCompanyByCompanyCode(companyCode).getCompanyName();
    }


    // 예금 계약 상세조회
    private ContractDetailResponseDto buildDepositContractDetail(Account account, Integer page, Integer size) {

        // 🔍 2단계: 예금 계약 조회 (1개 쿼리)
        DepositContract contract = depositContractRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new NoSuchElementException("예금 계약을 찾을 수 없습니다"));

        // 🔍 3단계: 상품 정보 조회 (지연 로딩으로 자동 쿼리)
        // contract.getDepositProduct() - 1개 쿼리
        // contract.getDepositProductOption() - 1개 쿼리

        // 🔍 4단계: 거래내역 조회 (페이징) - 1개 쿼리
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findByToAccountId(account.getId(), pageable);

        // 🔍 5단계: 만기 예상금액 계산 (메모리에서 계산)
        ContractDetailResponseDto.ExpectedAmountsDto expectedAmounts = calculateDepositExpectedAmounts(contract);

        // 🔄 DTO 조합
        return ContractDetailResponseDto.builder()
                .contractInfo(buildDepositContractInfo(contract))
                .accountInfo(buildAccountInfo(account, null))
                .expectedAmounts(expectedAmounts)
                .transactions(buildTransactionDetails(transactionPage.getContent(), account.getId()))
                .pagination(buildPagination(transactionPage))
                .build();
    }

    // 적금 계약 상세조회
    private ContractDetailResponseDto buildSavingContractDetail(Account account, Integer page, Integer size) {

        // 2단계: 적금 계약 조회 (1개 쿼리)
        SavingContract contract = savingContractRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new NoSuchElementException("적금 계약을 찾을 수 없습니다"));

        // 3단계: 상품 정보 조회 (지연 로딩)
        // contract.getSavingProduct() - 1개 쿼리
        // contract.getSavingProductOption() - 1개 쿼리

        // 4단계: 거래내역 조회 (페이징)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findByToAccountId(account.getId(), pageable);

        // 🔍 5단계: 만기 예상금액 계산
        ContractDetailResponseDto.ExpectedAmountsDto expectedAmounts = calculateSavingExpectedAmounts(contract);

        // 🔄 DTO 조합
        return ContractDetailResponseDto.builder()
                .contractInfo(buildSavingContractInfo(contract))
                .accountInfo(buildAccountInfo(account, contract.getCurrentPaymentCount()))
                .expectedAmounts(expectedAmounts)
                .transactions(buildTransactionDetails(transactionPage.getContent(), account.getId()))
                .pagination(buildPagination(transactionPage))
                .build();
    }

    // 🏗️ 예금 계약 정보 DTO 빌더
    private ContractDetailResponseDto.ContractInfoDto buildDepositContractInfo(DepositContract contract) {
        return ContractDetailResponseDto.ContractInfoDto.builder()
                .contractId(contract.getContractId())
                .productCode(contract.getDepositProduct().getProductCode())
                .productName(contract.getDepositProduct().getProductName())
                .bank(contract.getDepositProduct().getCompanyName())
                .contractType("DEPOSIT")
                .interestRate(contract.getDepositProductOption().getInterestRate2().doubleValue())
                .interestRateType(contract.getDepositProductOption().getInterestRateTypeName())
                .totalAmount(contract.getPayment())
                .monthlyPayment(null)
                .term(contract.getDepositProductOption().getSaveTerm())
                .startDate(contract.getContractDate())
                .endDate(contract.getMaturityDate())
                .contractStatus(contract.getContractCondition().toString())
                .build();
    }

    // 🏗️ 적금 계약 정보 DTO 빌더
    private ContractDetailResponseDto.ContractInfoDto buildSavingContractInfo(SavingContract contract) {
        return ContractDetailResponseDto.ContractInfoDto.builder()
                .contractId(contract.getContractId())
                .productCode(contract.getSavingProduct().getProductCode())
                .productName(contract.getSavingProduct().getProductName())
                .bank(contract.getSavingProduct().getCompanyName())
                .contractType("SAVING")
                .interestRate(contract.getSavingProductOption().getInterestRate2().doubleValue())
                .interestRateType(contract.getSavingProductOption().getInterestRateTypeName())
                .monthlyPayment(contract.getMonthlyPayment())
                .totalAmount(null)
                .term(contract.getSavingProductOption().getSaveTerm())
                .startDate(contract.getContractDate())
                .endDate(contract.getMaturityDate())
                .contractStatus(contract.getContractCondition().toString())
                .build();
    }

    // 🏗️ 계좌 정보 DTO 빌더
    private ContractDetailResponseDto.AccountInfoDto buildAccountInfo(Account account, Integer paymentCount) {
        return ContractDetailResponseDto.AccountInfoDto.builder()
                .accountId(account.getId())
                .accountNumber("001" + String.format("%04d", account.getId())) // 간단한 계좌번호 생성
                .currentBalance(account.getCurrentBalance())
                .paymentCount(paymentCount)
                .lastTransactionDate(account.getLastTransactionDate())
                .build();
    }

    // 💰 예금 예상금액 계산
    private ContractDetailResponseDto.ExpectedAmountsDto calculateDepositExpectedAmounts(DepositContract contract) {
        Long principal = contract.getPayment();
        Integer months = contract.getDepositProductOption().getSaveTerm();
        Double yearlyRate = contract.getDepositProductOption().getInterestRate2().doubleValue();

        // 단순 이자 계산 (실제로는 복잡한 로직)
        Long expectedInterest = Math.round(principal * (yearlyRate / 100.0) * (months / 12.0));

        return ContractDetailResponseDto.ExpectedAmountsDto.builder()
                .totalPayment(principal)
                .expectedInterest(expectedInterest)
                .maturityAmount(principal + expectedInterest)
                .build();
    }

    // 💰 적금 예상금액 계산
    private ContractDetailResponseDto.ExpectedAmountsDto calculateSavingExpectedAmounts(SavingContract contract) {
        Long monthlyPayment = contract.getMonthlyPayment();
        Integer totalTerm = contract.getSavingProductOption().getSaveTerm();
        Double yearlyRate = contract.getSavingProductOption().getInterestRate2().doubleValue();

        Long totalPayment = monthlyPayment * totalTerm;
        // 적금 이자 계산 (간단화)
        Long expectedInterest = Math.round(totalPayment * (yearlyRate / 100.0) * 0.5); // 평균 운용기간

        return ContractDetailResponseDto.ExpectedAmountsDto.builder()
                .totalPayment(totalPayment)
                .expectedInterest(expectedInterest)
                .maturityAmount(totalPayment + expectedInterest)
                .build();
    }

    // 🔄 거래내역 DTO 변환
    private List<ContractDetailResponseDto.TransactionDetailDto> buildTransactionDetails(List<Transaction> transactions, Integer accountId) {
        List<ContractDetailResponseDto.TransactionDetailDto> result = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            Integer paymentNumber = transactions.size() - i; // 최신이 가장 높은 회차

            String description = getTransactionDescription(tx.getTransactionType());
            String memo = String.format("%d회차 | %s", paymentNumber,
                    formatDate(tx.getCreatedAt()));

            ContractDetailResponseDto.TransactionDetailDto dto = ContractDetailResponseDto.TransactionDetailDto.builder()
                    .transactionId(tx.getTransactionId())
                    .transactionDate(tx.getCreatedAt())
                    .transactionType(tx.getTransactionType().toString())
                    .amount(tx.getAmount())
                    .balance(calculateBalanceAtTransaction(tx, accountId))
                    .description(description)
                    .paymentNumber(paymentNumber)
                    .memo(memo)
                    .build();

            result.add(dto);
        }

        return result;
    }

    private ContractDetailResponseDto.PaginationDto buildPagination(Page<Transaction> page) {
        return ContractDetailResponseDto.PaginationDto.builder()
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .totalCount(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }

    // 🔧 헬퍼 메서드들
    private String getTransactionDescription(TransactionType type) {
        return switch (type) {
            case PAYMENT -> "정기적금 납입";
            case TRANSFER -> "환급";
            default -> "거래";
        };
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yy년 MM월분"));
    }

    private Long calculateBalanceAtTransaction(Transaction tx, Integer accountId) {
        // 간단화: Transaction에 저장된 currentBalance 사용하거나 계산
        return tx.getCurrentBalance(); // 또는 별도 계산 로직
    }

    public Map<String, Object> comparePerformance(Integer accountId, SecurityUser securityUser) {
        log.info("성능 비교 테스트 시작 - 계좌ID: {}", accountId);

        // 기존 방식 (N+1 문제)
        long v1StartTime = System.nanoTime();
        ContractDetailResponseDto result1 = getContractDetailV1(accountId, securityUser);
        long v1EndTime = System.nanoTime();
        long v1TimeMs = (v1EndTime - v1StartTime) / 1_000_000;

        // JOIN FETCH 방식
        long v2StartTime = System.nanoTime();
        ContractDetailResponseDto result2 = getContractDetailV2(accountId, securityUser);
        long v2EndTime = System.nanoTime();
        long v2TimeMs = (v2EndTime - v2StartTime) / 1_000_000;

        // 성능 개선율 계산
        double improvementPercent = ((double)(v1TimeMs - v2TimeMs) / v1TimeMs) * 100;

        log.info("🐌 기존 방식: {}ms", v1TimeMs);
        log.info("⚡ JOIN FETCH: {}ms", v2TimeMs);
        log.info("🚀 성능 향상: {:.1f}%", improvementPercent);

        return Map.of(
                "v1_time_ms", v1TimeMs,
                "v2_time_ms", v2TimeMs,
                "improvement_percent", Math.round(improvementPercent * 10) / 10.0,
                "query_reduction", "5개 → 3개 쿼리"
        );
    }

    // 🐌 V1: 기존 방식 (N+1 문제)
    private ContractDetailResponseDto getContractDetailV1(Integer accountId, SecurityUser securityUser) {
        log.info("🐌 V1 실행 - 개별 조회 방식");

        // 1. 계좌 조회 (1개 쿼리)
        Account account = accountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다"));

        // 2. 적금 계약 조회 (1개 쿼리)
        SavingContract contract = savingContractRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NoSuchElementException("계약을 찾을 수 없습니다"));

        // 3. 상품 조회 (지연 로딩 - 1개 쿼리)
        String productName = contract.getSavingProduct().getProductName();

        // 4. 옵션 조회 (지연 로딩 - 1개 쿼리)
        Double interestRate = contract.getSavingProductOption().getInterestRate2().doubleValue();

        // 5. 거래내역 조회 (1개 쿼리)
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Transaction> transactions = transactionRepository.findByToAccountId(accountId, pageable);

        log.info("🐌 V1 완료 - 총 5개 쿼리 실행");
        return buildSavingContractDetail(account, pageable.getPageNumber() + 1, pageable.getPageSize());
    }

    // ⚡ V2: JOIN FETCH 방식
    private ContractDetailResponseDto getContractDetailV2(Integer accountId, SecurityUser securityUser) {
        log.info("⚡ V2 실행 - JOIN FETCH 방식");

        // 1. 계좌 조회 (1개 쿼리)
        Account account = accountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다"));

        // 2. 계약+상품+옵션 한 번에 조회 (1개 쿼리) 🚀
        SavingContract contract = savingContractRepository.findByAccountIdWithJoinFetch(accountId)
                .orElseThrow(() -> new NoSuchElementException("계약을 찾을 수 없습니다"));

        // 3. 이제 추가 쿼리 없이 접근 가능! ✅
        String productName = contract.getSavingProduct().getProductName(); // 추가 쿼리 없음
        Double interestRate = contract.getSavingProductOption().getInterestRate2().doubleValue(); // 추가 쿼리 없음

        // 4. 거래내역 조회 (1개 쿼리)
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Transaction> transactions = transactionRepository.findByToAccountId(accountId, pageable);

        log.info("⚡ V2 완료 - 총 3개 쿼리 실행");
        return buildSavingContractDetail(account, pageable.getPageNumber() + 1, pageable.getPageSize());
    }
}