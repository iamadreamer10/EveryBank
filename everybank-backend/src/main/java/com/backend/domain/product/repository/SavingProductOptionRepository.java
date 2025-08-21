package com.backend.domain.product.repository;

import com.backend.domain.product.domain.SavingProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingProductOptionRepository extends JpaRepository<SavingProductOption, Long> {
    List<SavingProductOption> findBySavingProductProductCode(String productCode);
}