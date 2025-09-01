package com.backend.domain.contract.service;

import com.backend.domain.account.domain.Account;
import com.backend.domain.account.domain.AccountType;
import com.backend.domain.account.repository.AccountRepository;
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
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractDetailService {

    private final AccountRepository accountRepository;
    private final DepositContractRepository depositContractRepository;
    private final SavingContractRepository savingContractRepository;
    private final TransactionRepository transactionRepository;

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
        if (account.getAccountType() == AccountType.DEPOSIT) {
            return buildDepositContractDetail(account, page, size);
        } else if (account.getAccountType() == AccountType.SAVING) {
            return buildSavingContractDetail(account, page, size);
        }

        throw new IllegalArgumentException("지원하지 않는 계좌 타입: " + account.getAccountType());
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
}