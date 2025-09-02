package com.backend.domain.company.repository;

import com.backend.domain.company.domain.FinCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface FinCompanyRepository extends JpaRepository<FinCompany, Long> {
    List<FinCompany> findByCompanyCodeIn(Set<String> companyCodes);

}
