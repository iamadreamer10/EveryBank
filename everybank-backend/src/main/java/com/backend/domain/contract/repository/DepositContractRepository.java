package com.backend.domain.contract.repository;

import com.backend.domain.contract.domain.DepositContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositContractRepository extends JpaRepository<DepositContract, Long> {
    Optional<DepositContract> findByAccountId(Integer accountId);
}
