package com.backend.domain.contract.repository;

import com.backend.domain.contract.domain.SavingContract;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingContractRepository extends JpaRepository<SavingContract, Long> {
    Optional<SavingContract> findByAccountId(Integer accountId);

    @Query("SELECT sc FROM SavingContract sc " +
            "JOIN FETCH sc.savingProduct " +
            "WHERE sc.accountId IN :accountIds")
    List<SavingContract> findByAccountIdIn(@Param("accountIds") List<Integer> accountIds);


    @Query("SELECT sc FROM SavingContract sc " +
            "JOIN FETCH sc.savingProduct sp " +
            "JOIN FETCH sc.savingProductOption spo " +
            "WHERE sc.accountId = :accountId")
    Optional<SavingContract> findByAccountIdWithJoinFetch(@Param("accountId") Integer accountId);
}
