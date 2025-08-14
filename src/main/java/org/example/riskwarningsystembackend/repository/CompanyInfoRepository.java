package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {

    Optional<CompanyInfo> findByName(String name);

    @Query("SELECT COUNT(DISTINCT c.industry) FROM CompanyInfo c WHERE c.industry IS NOT NULL AND c.industry <> ''")
    long countDistinctIndustry();

    // 用于风险分布
    long countByLegalDisputeCountGreaterThanEqual(int count);

    long countByLegalDisputeCountBetween(int start, int end);

    long countByLegalDisputeCountLessThanEqual(int count);

    // 用于行业健康状况
    @Query(value = "SELECT industry FROM company_info WHERE industry IS NOT NULL AND industry <> '' GROUP BY industry ORDER BY COUNT(id) DESC LIMIT 15", nativeQuery = true)
    List<String> findTop15IndustriesByCompanyCount();

    @Query("SELECT AVG(c.legalDisputeCount) FROM CompanyInfo c WHERE c.industry = :industry")
    Double findAverageLegalDisputesByIndustry(@Param("industry") String industry);

    // 用于供应链风险
    @Query("SELECT AVG(c.publicOpinionCount) FROM CompanyInfo c WHERE c.industry LIKE %:industry%")
    Double findAveragePublicOpinionByIndustry(@Param("industry") String industry);

    @Query("SELECT count(c) FROM CompanyInfo c WHERE (c.revenue = '0' OR c.profit = '0' OR c.revenue IS NULL OR c.profit IS NULL) AND c.industry LIKE %:industry%")
    long countFinancialRiskCompaniesByIndustry(@Param("industry") String industry);

    @Query("SELECT count(c) FROM CompanyInfo c WHERE c.industry LIKE %:industry%")
    long countByIndustry(@Param("industry") String industry);

    // 用于风险分析
    @Query("SELECT c FROM CompanyInfo c WHERE c.legalDisputeCount > 100 OR c.revenue = '0' OR c.profit = '0' OR c.revenue IS NULL OR c.profit IS NULL")
    Page<CompanyInfo> findHighRiskCompanies(Pageable pageable);

    // 用于风险地图
    @Query("SELECT c FROM CompanyInfo c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<CompanyInfo> findAllWithCoordinates();

    // 用于供应链搜索
    Page<CompanyInfo> findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(String name, String industry, Pageable pageable);

    /**
     * 根据公司名称进行模糊查询，用于知识图谱的搜索功能。
     * @param name 公司名称关键词
     * @return 匹配的公司信息列表
     */
    List<CompanyInfo> findByNameContainingIgnoreCase(String name);
}

