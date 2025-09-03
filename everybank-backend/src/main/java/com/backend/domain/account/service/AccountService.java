package com.backend.domain.account.service;

import com.backend.domain.account.domain.Account;
import com.backend.domain.account.domain.AccountState;
import com.backend.domain.account.domain.AccountType;
import com.backend.domain.account.dto.*;
import com.backend.domain.company.domain.FinCompany;
import com.backend.domain.company.repository.FinCompanyRepository;
import com.backend.domain.contract.domain.DepositContract;
import com.backend.domain.contract.domain.SavingContract;
import com.backend.domain.contract.repository.DepositContractRepository;
import com.backend.domain.contract.repository.SavingContractRepository;
import com.backend.domain.transaction.dto.ExternalDepositRequestDto;
import com.backend.domain.transaction.dto.ExternalWithdrawRequestDto;
import com.backend.domain.transaction.dto.PaymentRequestDto;
import com.backend.domain.transaction.dto.RefundRequestDto;
import com.backend.domain.transaction.dto.TransactionResponseDto;
import com.backend.domain.account.repository.AccountRepository;
import com.backend.domain.transaction.domain.Transaction;
import com.backend.domain.transaction.domain.TransactionType;
import com.backend.domain.transaction.repository.TransactionRepository;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final FinCompanyRepository finCompanyRepository;
    private final DepositContractRepository depositContractRepository;
    private final SavingContractRepository savingContractRepository;
    private final TransactionRepository transactionRepository;

    public MyAccountListInfoDto getMyAccounts(Long userId) {
        // 1. 사용자의 모든 계좌 조회
        List<Account> accounts = accountRepository.findActiveAccounts(userId)
                .orElse(List.of());

        if (accounts.isEmpty()) {
            return MyAccountListInfoDto.builder()
                    .count(0)
                    .accountList(List.of())
                    .build();
        }

        // 2. 필요한 데이터 배치 조회
        Map<String, String> bankNames = loadBankNames(accounts);
        Map<Integer, ContractInfo> contractInfos = loadContractInfos(accounts);

        // 3. DTO 변환
        List<AccountInfoDto> accountInfoList = accounts.stream()
                .map(account -> buildAccountInfoDto(account, bankNames, contractInfos))
                .collect(Collectors.toList());

        return MyAccountListInfoDto.builder()
                .count(accountInfoList.size())
                .accountList(accountInfoList)
                .build();
    }

    // 은행명 배치 조회
    private Map<String, String> loadBankNames(List<Account> accounts) {
        Set<String> companyCodes = accounts.stream()
                .map(Account::getCompanyCode)
                .collect(Collectors.toSet());

        return finCompanyRepository.findByCompanyCodeIn(companyCodes)
                .stream()
                .collect(Collectors.toMap(
                        FinCompany::getCompanyCode,
                        FinCompany::getCompanyName
                ));
    }

    // 계약 정보 배치 조회
    private Map<Integer, ContractInfo> loadContractInfos(List<Account> accounts) {
        Map<Integer, ContractInfo> result = new HashMap<>();

        // 예금 계좌들의 계약 정보
        List<Integer> depositAccountIds = accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.DEPOSIT)
                .map(Account::getId)
                .collect(Collectors.toList());

        if (!depositAccountIds.isEmpty()) {
            List<DepositContract> depositContracts =
                    depositContractRepository.findByAccountIdIn(depositAccountIds);

            depositContracts.forEach(contract -> {
                result.put(contract.getAccountId(), ContractInfo.builder()
                        .productName(contract.getDepositProduct().getProductName())
                        .contractDate(contract.getContractDate())
                        .endDate(contract.getMaturityDate())
                        .build());
            });
        }

        // 적금 계좌들의 계약 정보
        List<Integer> savingAccountIds = accounts.stream()
                .filter(account -> account.getAccountType() == AccountType.SAVING)
                .map(Account::getId)
                .collect(Collectors.toList());

        if (!savingAccountIds.isEmpty()) {
            List<SavingContract> savingContracts =
                    savingContractRepository.findByAccountIdIn(savingAccountIds);

            savingContracts.forEach(contract -> {
                result.put(contract.getAccountId(), ContractInfo.builder()
                        .productName(contract.getSavingProduct().getProductName())
                        .contractDate(contract.getContractDate())
                        .endDate(contract.getMaturityDate())
                        .build());
            });
        }

        return result;
    }

    // DTO 빌드
    private AccountInfoDto buildAccountInfoDto(Account account,
                                               Map<String, String> bankNames,
                                               Map<Integer, ContractInfo> contractInfos) {

        String bankName = bankNames.getOrDefault(account.getCompanyCode(), "알 수 없음");

        String productName;
        String startDate;
        String endDate;

        if (account.getAccountType() == AccountType.CHECK) {
            // 입출금계좌는 Contract가 없음
            productName = "입출금계좌";
            startDate = account.getLastTransactionDate() != null ?
                    account.getLastTransactionDate().toLocalDate()
                            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) : "";
            endDate = "2099.12.31";
        } else {
            // 예금/적금은 Contract에서 정보 가져오기
            ContractInfo contractInfo = contractInfos.get(account.getId());
            if (contractInfo != null) {
                productName = contractInfo.getProductName();
                startDate = contractInfo.getContractDate()
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                endDate = contractInfo.getEndDate()
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            } else {
                productName = "상품정보 없음";
                startDate = "";
                endDate = "";

            }
        }

        return AccountInfoDto.builder()
                .accountId(account.getId())
                .accountName(generateAccountName(account)) // 임시로 생성
                .balance(account.getCurrentBalance())
                .bank(bankName)
                .productName(productName)
                .accountType(account.getAccountType().toString())
                .startDate(startDate)
                .endDate(endDate)
                .status(account.getAccountState().toString().toLowerCase())
                .build();
    }

    // 임시 계좌명 생성 (나중에 DB 컬럼 추가 또는 사용자 설정 기능)
    private String generateAccountName(Account account) {
        return switch (account.getAccountType()) {
            case CHECK -> "내 입출금계좌";
            case DEPOSIT -> "내 예금계좌";
            case SAVING -> "내 적금계좌";
        };
    }


    // 입출금계좌 등록 메서드 (사용자당 1개만 허용)
    public CheckingAccountResponseDto registerCheckingAccount(CheckingAccountRequestDto requestDto, SecurityUser securityUser) {
        log.info("입출금계좌 등록 요청 - 사용자: {}, 은행: {}", securityUser.getId(), requestDto.getBankName());

        // 중요: 사용자당 입출금계좌는 1개만 허용
        boolean hasCheckingAccount = accountRepository.findByUserId(securityUser.getId())
                .orElse(List.of())
                .stream()
                .anyMatch(account -> account.getAccountType() == AccountType.CHECK &&
                        account.getAccountState() == AccountState.ACTIVE);

        if (hasCheckingAccount) {
            throw new IllegalArgumentException("이미 입출금계좌를 보유하고 있습니다. 사용자당 입출금계좌는 1개만 개설 가능합니다.");
        }

        log.info("입출금계좌 개설 가능 - 사용자: {}", securityUser.getId());

        // 입출금계좌 생성 (만기일 없음 - 99년 후로 설정)
        Account checkingAccount = Account.builder()
                .userId(securityUser.getId())
                .companyCode(requestDto.getCompanyCode())
                .currentBalance(0L) // 초기 잔액 0원
                .accountType(AccountType.CHECK)
                .accountState(AccountState.ACTIVE)
                .lastTransactionDate(LocalDateTime.now())
                .maturityDate(LocalDate.now().plusYears(99)) // 사실상 만기 없음
                .build();

        Account savedAccount = accountRepository.save(checkingAccount);
        log.info("입출금계좌 등록 완료 - 계좌ID: {}", savedAccount.getId());

        return CheckingAccountResponseDto.builder()
                .accountId(savedAccount.getId())
                .companyCode(savedAccount.getCompanyCode())
                .bankName(requestDto.getBankName())
                .accountType(savedAccount.getAccountType().toString())
                .currentBalance(savedAccount.getCurrentBalance())
                .accountState(savedAccount.getAccountState().toString())
                .contractDate(savedAccount.getLastTransactionDate())
                .build();
    }

    // 외부 → 입출금계좌 (외부입금)
    @Transactional
    public TransactionResponseDto externalDeposit(ExternalDepositRequestDto requestDto, SecurityUser securityUser) {
        log.info("외부입금 요청 - 사용자: {}, 금액: {}", securityUser.getId(), requestDto.getAmount());

        // 1. 금액 검증
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("입금액은 0원보다 커야 합니다.");
        }

        // 2. 사용자의 입출금계좌 찾기
        Account checkingAccount = findUserCheckingAccount(securityUser.getId());

        // 3. 잔액 업데이트
        checkingAccount.setCurrentBalance(checkingAccount.getCurrentBalance() + requestDto.getAmount());
        checkingAccount.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(checkingAccount);

        // 4. 거래내역 저장
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.DEPOSIT)
                .amount(requestDto.getAmount())
                .fromAccountId(null) // 외부
                .toAccountId(checkingAccount.getId())
                .currentBalance(checkingAccount.getCurrentBalance())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("외부입금 완료 - 거래ID: {}, 입출금계좌 잔액: {}", savedTransaction.getTransactionId(), checkingAccount.getCurrentBalance());

        return buildTransactionResponse(savedTransaction, checkingAccount.getCurrentBalance());
    }

    // 입출금계좌 → 외부 (외부출금)
    @Transactional
    public TransactionResponseDto externalWithdraw(ExternalWithdrawRequestDto requestDto, SecurityUser securityUser) {
        log.info("외부출금 요청 - 사용자: {}, 금액: {}", securityUser.getId(), requestDto.getAmount());

        // 1. 금액 검증
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("출금액은 0원보다 커야 합니다.");
        }

        // 2. 사용자의 입출금계좌 찾기
        Account checkingAccount = findUserCheckingAccount(securityUser.getId());

        // 3. 잔액 검증
        if (checkingAccount.getCurrentBalance() < requestDto.getAmount()) {
            throw new IllegalArgumentException("잔액이 부족합니다. 현재 잔액: " + checkingAccount.getCurrentBalance() + "원");
        }

        // 4. 잔액 업데이트
        checkingAccount.setCurrentBalance(checkingAccount.getCurrentBalance() - requestDto.getAmount());
        checkingAccount.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(checkingAccount);

        // 5. 거래내역 저장
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(requestDto.getAmount())
                .fromAccountId(checkingAccount.getId())
                .toAccountId(null) // 외부
                .currentBalance(checkingAccount.getCurrentBalance())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("외부출금 완료 - 거래ID: {}, 입출금계좌 잔액: {}", savedTransaction.getTransactionId(), checkingAccount.getCurrentBalance());

        return buildTransactionResponse(savedTransaction, checkingAccount.getCurrentBalance());
    }

    // 입출금계좌 → 금융상품 (납입)
    @Transactional
    public TransactionResponseDto paymentToProduct(PaymentRequestDto requestDto, SecurityUser securityUser) {
        log.info("납입 요청 - 사용자: {}, 상품계좌: {}, 금액: {}",
                securityUser.getId(), requestDto.getToAccountId(), requestDto.getAmount());

        // 1. 금액 검증
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("납입액은 0원보다 커야 합니다.");
        }

        // 2. 사용자의 입출금계좌 찾기
        Account checkingAccount = findUserCheckingAccount(securityUser.getId());

        // 3. 상품계좌 찾기 및 검증
        Account productAccount = accountRepository.findById(Long.valueOf(requestDto.getToAccountId()))
                .orElseThrow(() -> new NoSuchElementException("상품계좌를 찾을 수 없습니다: " + requestDto.getToAccountId()));

        // 4. 본인 계좌 검증
        if (!productAccount.getUserId().equals(securityUser.getId())) {
            throw new IllegalArgumentException("본인의 상품계좌만 납입 가능합니다.");
        }

        // 5. 상품계좌 타입 검증
        if (productAccount.getAccountType() == AccountType.CHECK) {
            throw new IllegalArgumentException("입출금계좌로는 납입할 수 없습니다.");
        }

        // 6. 상품계좌 상태 검증
        if (productAccount.getAccountState() != AccountState.ACTIVE) {
            throw new IllegalArgumentException("활성 상태의 상품계좌만 납입 가능합니다.");
        }

        // 7. 입출금계좌 잔액 검증
        if (checkingAccount.getCurrentBalance() < requestDto.getAmount()) {
            throw new IllegalArgumentException("잔액이 부족합니다. 현재 잔액: " + checkingAccount.getCurrentBalance() + "원");
        }

        // 8. 계좌 잔액 업데이트
        checkingAccount.setCurrentBalance(checkingAccount.getCurrentBalance() - requestDto.getAmount());
        checkingAccount.setLastTransactionDate(LocalDateTime.now());

        productAccount.setCurrentBalance(productAccount.getCurrentBalance() + requestDto.getAmount());
        productAccount.setLastTransactionDate(LocalDateTime.now());

        accountRepository.save(checkingAccount);
        accountRepository.save(productAccount);

        // 9. 적금인 경우 납입 횟수 증가 및 최근 납입일 업데이트
        if (productAccount.getAccountType() == AccountType.SAVING) {
            SavingContract savingContract = savingContractRepository.findByAccountId(productAccount.getId())
                    .orElseThrow(() -> new NoSuchElementException("적금 계약을 찾을 수 없습니다: " + productAccount.getId()));

            // 납입 횟수 증가
            savingContract.setCurrentPaymentCount(savingContract.getCurrentPaymentCount() + 1);
            savingContract.setLatestPaymentDate(LocalDate.now());

            savingContractRepository.save(savingContract);

            log.info("적금 납입 완료 - 계약ID: {}, 납입 횟수: {}/{}회, 납입액: {}원",
                    savingContract.getContractId(),
                    savingContract.getCurrentPaymentCount(),
                    savingContract.getSavingProductOption().getSaveTerm(),
                    requestDto.getAmount());
        }

        // 10. 거래내역 저장
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PAYMENT)
                .amount(requestDto.getAmount())
                .fromAccountId(checkingAccount.getId())
                .toAccountId(requestDto.getToAccountId())
                .currentBalance(checkingAccount.getCurrentBalance())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("납입 완료 - 거래ID: {}, 상품: {} → 입출금계좌 잔액: {}",
                savedTransaction.getTransactionId(), productAccount.getAccountType(), checkingAccount.getCurrentBalance());

        return buildTransactionResponse(savedTransaction, checkingAccount.getCurrentBalance());
    }

    // 금융상품 → 입출금계좌 (환급) - 전액 자동 환급
    @Transactional
    public TransactionResponseDto refundFromProduct(RefundRequestDto requestDto, SecurityUser securityUser) {
        log.info("전액 환급 요청 - 사용자: {}, 상품계좌: {}",
                securityUser.getId(), requestDto.getFromAccountId());

        // 1. 사용자의 입출금계좌 찾기
        Account checkingAccount = findUserCheckingAccount(securityUser.getId());

        // 2. 상품계좌 찾기 및 검증
        Account productAccount = accountRepository.findById(Long.valueOf(requestDto.getFromAccountId()))
                .orElseThrow(() -> new NoSuchElementException("상품계좌를 찾을 수 없습니다: " + requestDto.getFromAccountId()));

        // 3. 본인 계좌 검증
        if (!productAccount.getUserId().equals(securityUser.getId())) {
            throw new IllegalArgumentException("본인의 상품계좌만 환급 가능합니다.");
        }

        // 4. 상품계좌 타입 검증
        if (productAccount.getAccountType() == AccountType.CHECK) {
            throw new IllegalArgumentException("입출금계좌는 환급 대상이 아닙니다.");
        }

        // 5. 환급 가능 상태 검증
        if (productAccount.getAccountState() == AccountState.EARLY_CLOSED ||
                productAccount.getAccountState() == AccountState.EXPIRED) {
            throw new IllegalArgumentException("이미 해지된 상품계좌입니다.");
        }

        // 6. 환급할 전액 확인
        Long refundAmount = productAccount.getCurrentBalance();
        if (refundAmount <= 0) {
            throw new IllegalArgumentException("환급할 금액이 없습니다. 상품계좌 잔액: " + refundAmount + "원");
        }

        log.info("전액 환급 처리 - 상품계좌 잔액: {}원", refundAmount);

        // 7. 만기/중도해지 처리
        if (LocalDate.now().isBefore(productAccount.getMaturityDate())) {
            log.warn("중도해지 처리 - 상품계좌: {}, 만기일: {}", productAccount.getId(), productAccount.getMaturityDate());
            productAccount.setAccountState(AccountState.EARLY_CLOSED);
        } else {
            log.info("만기해지 처리 - 상품계좌: {}", productAccount.getId());
            productAccount.setAccountState(AccountState.EXPIRED);
        }

        // 8. 계좌 잔액 업데이트 (상품계좌 → 입출금계좌 전액 이동)
        productAccount.setCurrentBalance(0L); // 상품계좌 잔액 0으로
        productAccount.setLastTransactionDate(LocalDateTime.now());

        checkingAccount.setCurrentBalance(checkingAccount.getCurrentBalance() + refundAmount);
        checkingAccount.setLastTransactionDate(LocalDateTime.now());

        accountRepository.save(productAccount);
        accountRepository.save(checkingAccount);

        // 9. 거래내역 저장
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .amount(refundAmount) // 상품계좌의 전액
                .fromAccountId(requestDto.getFromAccountId())
                .toAccountId(checkingAccount.getId())
                .currentBalance(checkingAccount.getCurrentBalance())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("전액 환급 완료 - 거래ID: {}, 상품: {}, 환급액: {}원, 입출금계좌 잔액: {}원",
                savedTransaction.getTransactionId(), productAccount.getAccountType(),
                refundAmount, checkingAccount.getCurrentBalance());

        return buildTransactionResponse(savedTransaction, checkingAccount.getCurrentBalance());
    }

    // 사용자의 입출금계좌 찾기 (헬퍼 메서드)
    private Account findUserCheckingAccount(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElse(List.of())
                .stream()
                .filter(account -> account.getAccountType() == AccountType.CHECK &&
                        account.getAccountState() == AccountState.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("입출금계좌가 없습니다. 먼저 입출금계좌를 개설해주세요."));
    }

    // Transaction 응답 빌더 (헬퍼 메서드)
    private TransactionResponseDto buildTransactionResponse(Transaction transaction, Long checkingAccountBalance) {
        return TransactionResponseDto.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType().toString())
                .amount(transaction.getAmount())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .createdAt(transaction.getCreatedAt())
                .checkingAccountBalance(checkingAccountBalance)
                .build();
    }

}