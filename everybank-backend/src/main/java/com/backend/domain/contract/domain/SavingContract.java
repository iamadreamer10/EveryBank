package com.backend.domain.contract.domain;


import com.backend.domain.product.domain.DepositProduct;
import com.backend.domain.product.domain.DepositProductOption;
import com.backend.domain.product.domain.SavingProduct;
import com.backend.domain.product.domain.SavingProductOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "saving_contract")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingContract {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contract_seq")
    @SequenceGenerator(name = "saving_contract_seq", sequenceName = "saving_contract_seq_id", allocationSize = 1)
    private Long contractId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    private SavingProduct savingProduct;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private SavingProductOption savingProductOption;

    @Column(name = "monthly_payment", nullable = false)
    private Long monthlyPayment;

    @Column(name = "current_payment_count", nullable = false)
    @Builder.Default
    private Integer currentPaymentCount = 0;

    @Column(name = "latest_payment_date")
    private LocalDate latestPaymentDate;

    @Column(name = "contract_condition", nullable = false)
    private ContractCondition contractCondition;

    @Column(name = "account_id", nullable = false)
    private int accountId;

}