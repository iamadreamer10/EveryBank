package com.backend.domain.product.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "deposit_product_option")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "option_seq")
    @SequenceGenerator(name = "option_seq", sequenceName = "deposit_product_option_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    private DepositProduct depositProduct;

    @Column(name = "interest_rate_type", length = 1, nullable = false)
    private char interestRateType;

    @Column(name = "interest_rate_type_name", length = 20, nullable = false)
    private String interestRateTypeName;

    @Column(name = "save_term", nullable = false)
    private int saveTerm;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "interest_rate2", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate2;
}
