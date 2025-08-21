package com.backend.domain.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "saving_product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingProduct {

    @Id
    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_name",nullable = false)
    private String productName;

    @Column(name = "company_code",nullable = false)
    private String companyCode;

    @Column(name = "company_name",nullable = false)
    private String companyName;

    @Column(name = "join_member", nullable = false)
    private String joinMember;

    @Column(name = "etc_note")
    private String etcNote;

    @Column(name = "max_limit")
    private int maxLimit;

    @Column(name = "main_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal mainRate = BigDecimal.valueOf(0.00);

}
