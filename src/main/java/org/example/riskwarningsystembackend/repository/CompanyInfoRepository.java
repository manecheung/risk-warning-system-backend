package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    @Query("SELECT COUNT(DISTINCT c.industry) FROM CompanyInfo c")
    long countDistinctIndustry();
}
