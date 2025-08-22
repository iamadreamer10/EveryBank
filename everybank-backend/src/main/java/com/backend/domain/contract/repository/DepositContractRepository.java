package com.backend.domain.contract.repository;

import com.backend.domain.contract.domain.DepositContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositContractRepository extends JpaRepository<DepositContract, Long> {

}
