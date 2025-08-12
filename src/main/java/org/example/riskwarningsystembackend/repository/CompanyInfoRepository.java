package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    @Query("SELECT COUNT(DISTINCT c.industry) FROM CompanyInfo c WHERE c.industry IS NOT NULL AND c.industry <> ''")
    long countDistinctIndustry();

    // For Risk Distribution
    long countByLegalDisputeCountGreaterThanEqual(int count);
    long countByLegalDisputeCountBetween(int start, int end);
    long countByLegalDisputeCountLessThanEqual(int count);

    // For Industry Health
        @Query(value = "SELECT industry FROM company_info WHERE industry IS NOT NULL AND industry <> '' GROUP BY industry ORDER BY COUNT(id) DESC LIMIT 10", nativeQuery = true)
    List<String> findTop10IndustriesByCompanyCount();

    @Query("SELECT AVG(c.legalDisputeCount) FROM CompanyInfo c WHERE c.industry = :industry")
    Double findAverageLegalDisputesByIndustry(@Param("industry") String industry);

    // For Supply Chain Risk
    @Query("SELECT AVG(c.publicOpinionCount) FROM CompanyInfo c WHERE c.industry LIKE %:industry%")
    Double findAveragePublicOpinionByIndustry(@Param("industry") String industry);

    @Query("SELECT count(c) FROM CompanyInfo c WHERE (c.revenue = '0' OR c.profit = '0' OR c.revenue IS NULL OR c.profit IS NULL) AND c.industry LIKE %:industry%")
    long countFinancialRiskCompaniesByIndustry(@Param("industry") String industry);

    @Query("SELECT count(c) FROM CompanyInfo c WHERE c.industry LIKE %:industry%")
    long countByIndustry(@Param("industry") String industry);

    // For Risk Analysis
    @Query("SELECT c FROM CompanyInfo c WHERE c.legalDisputeCount > 100 OR c.revenue = '0' OR c.profit = '0' OR c.revenue IS NULL OR c.profit IS NULL")
    Page<CompanyInfo> findHighRiskCompanies(Pageable pageable);

    // For Risk Map
    @Query("SELECT c FROM CompanyInfo c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<CompanyInfo> findAllWithCoordinates();

    // For Supply Chain Search
    Page<CompanyInfo> findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(String name, String industry, Pageable pageable);
}

