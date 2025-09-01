package com.backend.domain.contract.repository;

import com.backend.domain.contract.domain.SavingContract;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingContractRepository extends JpaRepository<SavingContract, Long> {
    Optional<SavingContract> findByAccountId(Integer accountId);

}
