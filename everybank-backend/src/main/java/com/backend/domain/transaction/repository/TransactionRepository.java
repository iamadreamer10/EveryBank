package com.backend.domain.transaction.repository;

import com.backend.domain.transaction.domain.Transaction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByToAccountId(int id, Pageable pageable);

    // 입출금계좌용 거래내역 조회 (양방향)
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findCheckingAccountTransactions(
            @Param("accountId") Integer accountId,
            Pageable pageable);

    // 입출금계좌의 모든 거래내역 (페이징 없이)
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findAllCheckingAccountTransactions(
            @Param("accountId") Integer accountId);

    // 특정 기간의 입출금계좌 거래내역
    @Query("SELECT t FROM Transaction t " +
            "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findCheckingAccountTransactionsByDateRange(
            @Param("accountId") Integer accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}