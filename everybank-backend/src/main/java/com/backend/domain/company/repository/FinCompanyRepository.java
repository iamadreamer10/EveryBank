package com.backend.domain.company.repository;

import com.backend.domain.company.domain.FinCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinCompanyRepository extends JpaRepository<FinCompany, Long> {
}
