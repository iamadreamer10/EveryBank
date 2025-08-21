package com.backend.domain.product.repository;

import com.backend.domain.product.domain.DepositProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {

    DepositProduct findByProductCode(String productCode);

}
