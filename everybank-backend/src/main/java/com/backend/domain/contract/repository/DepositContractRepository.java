package com.backend.domain.contract.repository;

import com.backend.domain.contract.domain.DepositContract;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositContractRepository extends JpaRepository<DepositContract, Long> {
    Optional<DepositContract> findByAccountId(Integer accountId);


    @Query("SELECT dc FROM DepositContract dc " +
            "JOIN FETCH dc.depositProduct " +
            "WHERE dc.accountId IN :accountIds")
    List<DepositContract> findByAccountIdIn(@Param("accountIds") List<Integer> accountIds);
}
