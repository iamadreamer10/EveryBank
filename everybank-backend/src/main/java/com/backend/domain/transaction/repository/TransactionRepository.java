package com.backend.domain.transaction.repository;

import com.backend.domain.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 특정 계좌의 거래내역 조회
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(Integer fromAccountId, Integer toAccountId);

    // 특정 계좌에서 출금한 거래내역
    List<Transaction> findByFromAccountIdOrderByCreatedAtDesc(Integer fromAccountId);

}