package com.backend.config;

import com.backend.domain.account.domain.Account;
import com.backend.domain.account.domain.AccountState;
import com.backend.domain.account.domain.AccountType;
import com.backend.domain.account.repository.AccountRepository;
import com.backend.domain.contract.domain.ContractCondition;
import com.backend.domain.contract.domain.SavingContract;
import com.backend.domain.contract.repository.SavingContractRepository;
import com.backend.domain.product.domain.SavingProduct;
import com.backend.domain.product.domain.SavingProductOption;
import com.backend.domain.product.repository.SavingProductOptionRepository;
import com.backend.domain.product.repository.SavingProductRepository;
import com.backend.domain.transaction.domain.Transaction;
import com.backend.domain.transaction.domain.TransactionType;
import com.backend.domain.transaction.repository.TransactionRepository;
import com.backend.domain.user.domain.User;
import com.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class TestDataConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final SavingContractRepository savingContractRepository;
    private final TransactionRepository transactionRepository;
    private final SavingProductRepository savingProductRepository;
    private final SavingProductOptionRepository savingProductOptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail("onyo9@naver.com")) {
            log.info("권원영님 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        // 🔍 상품 데이터 확인 후 없으면 나중에 다시 실행하도록 안내
        List<SavingProduct> products = savingProductRepository.findAll();
        if (products.isEmpty()) {
            log.warn("⚠️ 상품 데이터가 아직 로드되지 않았습니다.");
            log.warn("⚠️ OpenAPI 데이터 로딩 완료 후 애플리케이션을 재시작해주세요.");
            return;
        }

        log.info("=== 권원영님 만기정산 테스트 데이터 생성 시작 ===");
        log.info("✅ 상품 데이터 확인 완료: {}개 상품 발견", products.size());

        try {
            createMaturityTestData();
            log.info("=== 만기정산 테스트 데이터 생성 완료 ===");
        } catch (Exception e) {
            log.error("권원영님 데이터 생성 실패", e);
        }
    }

    private void createMaturityTestData() {
        // 1. 권원영님 회원가입
        User wonYoung = createWonYoungUser();

        // 2. 입출금계좌 개설
        Account checkingAccount = createCheckingAccount(wonYoung.getId());

        // 3. 오늘 만료되는 적금계좌 생성 (ACTIVE 상태)
        Account maturitySavingAccount = createMaturitySavingAccount(wonYoung.getId());
        SavingContract maturitySavingContract = createMaturitySavingContract(wonYoung.getId(), maturitySavingAccount.getId());

        // 4. 적금 납입 내역 생성 (24개월간 성실 납입)
        createMaturitySavingPaymentHistory(checkingAccount, maturitySavingAccount);

        // 5. 입출금계좌 입금 내역 (충분한 자금 확보)
        createCheckingAccountDeposits(checkingAccount);

        log.info("🎉 권원영님 만기정산 테스트 데이터 생성 완료:");
        log.info("📧 이메일: {} (비밀번호: 1234)", wonYoung.getEmail());
        log.info("💳 입출금계좌 ID: {} (잔액: {}원)", checkingAccount.getId(), checkingAccount.getCurrentBalance());
        log.info("🔥 오늘 만료 적금 ID: {} (잔액: {}원, 상태: {})",
                maturitySavingAccount.getId(), maturitySavingAccount.getCurrentBalance(), maturitySavingAccount.getAccountState());
        log.info("📅 적금 만료일: {} (오늘!)", maturitySavingAccount.getMaturityDate());
        log.info("✅ 만기정산 API 테스트 준비 완료!");
    }

    private User createWonYoungUser() {
        User user = User.builder()
                .email("onyo9@naver.com")
                .password(passwordEncoder.encode("1234"))
                .nickname("권원영")
                .birthdate(LocalDate.of(1995, 9, 29))
                .build();
        return userRepository.save(user);
    }

    private Account createCheckingAccount(Long userId) {
        Account account = Account.builder()
                .userId(userId)
                .companyCode("0010001") // KB국민은행
                .currentBalance(10000000L) // 1천만원 (충분한 잔액)
                .accountType(AccountType.CHECK)
                .accountState(AccountState.ACTIVE)
                .lastTransactionDate(LocalDateTime.now().minusDays(1))
                .maturityDate(LocalDate.now().plusYears(99)) // 입출금계좌는 만료 없음
                .build();
        return accountRepository.save(account);
    }

    // 🔥 오늘 만료되는 적금계좌 생성 (먼저!)
    private Account createMaturitySavingAccount(Long userId) {
        Account account = Account.builder()
                .userId(userId)
                .companyCode("0010002") // 신한은행
                .currentBalance(12000000L) // 50만원 × 24개월 = 1200만원 원금
                .accountType(AccountType.SAVING)
                .accountState(AccountState.ACTIVE) // 🔥 아직 ACTIVE 상태!
                .lastTransactionDate(LocalDateTime.now().minusMonths(1)) // 한달 전 마지막 납입
                .maturityDate(LocalDate.now()) // 🔥 오늘이 만료일!
                .build();
        return accountRepository.save(account);
    }

    // 계좌 기반으로 적금계약 생성
    private SavingContract createMaturitySavingContract(Long userId, Integer accountId) {
        List<SavingProduct> savingProducts = savingProductRepository.findAll();
        if (savingProducts.isEmpty()) {
            throw new RuntimeException("적금 상품이 없습니다. 먼저 OpenAPI 데이터를 로드해주세요.");
        }

        SavingProduct savingProduct = savingProducts.get(0);
        SavingProductOption option = savingProductOptionRepository
                .findBySavingProductProductCode(savingProduct.getProductCode())
                .stream()
                .filter(opt -> opt.getSaveTerm() >= 24) // 24개월 이상 상품 선택
                .findFirst()
                .orElse(savingProductOptionRepository
                        .findBySavingProductProductCode(savingProduct.getProductCode())
                        .stream().findFirst().orElseThrow(() -> new RuntimeException("적금 상품 옵션이 없습니다.")));

        SavingContract contract = SavingContract.builder()
                .userId(userId)
                .savingProduct(savingProduct)
                .contractDate(LocalDate.now().minusMonths(24)) // 24개월 전 계약
                .maturityDate(LocalDate.now()) // 🔥 오늘이 만료일!
                .savingProductOption(option)
                .monthlyPayment(500000L) // 월 50만원
                .currentPaymentCount(24) // 24회 완납
                .latestPaymentDate(LocalDate.now().minusMonths(1)) // 한달 전 마지막 납입
                .contractCondition(ContractCondition.IN_PROGRESS) // 🔥 아직 진행중 상태!
                .accountId(accountId) // 이미 생성된 계좌 ID 참조
                .build();
        return savingContractRepository.save(contract);
    }

    // 적금 납입 내역 생성 (24개월간 성실 납입)
    private void createMaturitySavingPaymentHistory(Account checkingAccount, Account savingAccount) {
        LocalDate startDate = LocalDate.now().minusMonths(24); // 24개월 전부터 시작

        for (int i = 0; i < 24; i++) {
            LocalDate paymentDate = startDate.plusMonths(i);
            LocalDateTime paymentTime = paymentDate.atTime(10, 0); // 매월 10시에 납입

            Transaction tx = Transaction.builder()
                    .transactionType(TransactionType.PAYMENT)
                    .amount(500000L) // 월 50만원
                    .fromAccountId(checkingAccount.getId())
                    .toAccountId(savingAccount.getId())
                    .createdAt(paymentTime)
                    .currentBalance(500000L * (i + 1)) // 적금 계좌 잔액 누적
                    .build();
            transactionRepository.save(tx);
        }

        log.info("💰 적금 납입 내역 생성 완료: 월 50만원 × 24회 = 총 {}원", 500000L * 24);
    }

    // 입출금계좌 입금 내역 생성 (충분한 자금 확보)
    private void createCheckingAccountDeposits(Account checkingAccount) {
        // 월급 등 정기 입금
        LocalDate startDate = LocalDate.now().minusMonths(24);

        for (int i = 0; i < 24; i++) {
            LocalDate depositDate = startDate.plusMonths(i);
            LocalDateTime depositTime = depositDate.atTime(9, 0); // 매월 초 월급 입금

            Transaction tx = Transaction.builder()
                    .transactionType(TransactionType.DEPOSIT)
                    .amount(6000000L) // 월급 600만원
                    .fromAccountId(null) // 외부에서
                    .toAccountId(checkingAccount.getId())
                    .createdAt(depositTime)
                    .currentBalance(checkingAccount.getCurrentBalance())
                    .build();
            transactionRepository.save(tx);
        }

        log.info("💸 입출금계좌 입금 내역 생성 완료: 월급 + 보너스");
    }
}