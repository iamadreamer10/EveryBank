package com.backend.domain.company.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "fin_company")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinCompany {

    @Id
    private String companyCode;

    @Column(name = "company_name", nullable = false)
    private String companyName;

}
