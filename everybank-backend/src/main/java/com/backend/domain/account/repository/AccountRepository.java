package com.backend.domain.account.repository;

import com.backend.domain.account.domain.Account;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<List<Account>> findByUserId(Long userId);

    @Query("select a from Account a " +
            "where a.userId = :userId " +
            "and a.accountState = 'ACTIVE'")
    Optional<List<Account>> findActiveAccounts(@Param("userId") Long userId);
}
