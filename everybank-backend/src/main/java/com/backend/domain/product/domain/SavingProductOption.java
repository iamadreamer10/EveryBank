package com.backend.domain.product.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "saving_product_option")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "option_seq")
    @SequenceGenerator(name = "option_seq", sequenceName = "saving_product_option_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    private SavingProduct savingProduct;

    @Column(name = "interest_rate_type", length = 1, nullable = false)
    private char interestRateType;

    @Column(name = "interest_rate_type_name", length = 20, nullable = false)
    private String interestRateTypeName;

    @Column(name = "reverse_type", length = 1, nullable = false)
    private char reverseType;

    @Column(name = "reverse_type_name", length = 20, nullable = false)
    private String reverseTypeName;

    @Column(name = "save_term", nullable = false)
    private int saveTerm;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "interest_rate2", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate2;
}
