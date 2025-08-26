package com.backend.domain.contract.domain;


import com.backend.domain.product.domain.DepositProduct;
import com.backend.domain.product.domain.DepositProductOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "deposit_contract")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositContract {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_seq")
    @SequenceGenerator(name = "contract_seq", sequenceName = "deposit_contract_seq_id", allocationSize = 1)
    private Long contractId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    private DepositProduct depositProduct;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private DepositProductOption depositProductOption;

    @Column(name = "payment", nullable = false)
    private Long payment;

    @Column(name = "contract_condition", nullable = false)
    private ContractCondition contractCondition;

    @Column(name = "account_id", nullable = false)
    private int accountId;

}