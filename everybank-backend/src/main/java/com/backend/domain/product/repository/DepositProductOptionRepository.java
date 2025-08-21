package com.backend.domain.product.repository;

import com.backend.domain.product.domain.DepositProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositProductOptionRepository extends JpaRepository<DepositProductOption, Long> {
    List<DepositProductOption> findByDepositProductProductCode(String productCode);
}