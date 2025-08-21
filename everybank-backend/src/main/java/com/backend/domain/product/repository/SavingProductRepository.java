package com.backend.domain.product.repository;

import com.backend.domain.product.domain.SavingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingProductRepository extends JpaRepository<SavingProduct, Long> {

    SavingProduct findByProductCode(String productCode);
}
