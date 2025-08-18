package org.example.riskwarningsystembackend.repository.company;

import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 公司信息数据访问接口，提供对公司信息的增删改查及复杂查询功能。
 * 继承自JpaRepository，支持分页、排序等通用操作。
 */
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {

    /**
     * 根据公司名称查找公司信息。
     * @param name 公司名称
     * @return 匹配的公司信息（Optional包装）
     */
    Optional<CompanyInfo> findByName(String name);

    /**
     * 查询所有非空且非空字符串的行业种类数量。
     * @return 不重复的行业数量
     */
    @Query("SELECT COUNT(DISTINCT c.industry) FROM CompanyInfo c WHERE c.industry IS NOT NULL AND c.industry <> ''")
    long countDistinctIndustry();

    // 用于风险分布

    /**
     * 统计法律纠纷数大于等于指定值的公司数量。
     * @param count 法律纠纷数下限
     * @return 满足条件的公司数量
     */
    long countByLegalDisputeCountGreaterThanEqual(int count);

    /**
     * 统计法律纠纷数在指定区间内的公司数量。
     * @param start 起始值（包含）
     * @param end 结束值（包含）
     * @return 满足条件的公司数量
     */
    long countByLegalDisputeCountBetween(int start, int end);

    /**
     * 统计法律纠纷数小于等于指定值的公司数量。
     * @param count 法律纠纷数上限
     * @return 满足条件的公司数量
     */
    long countByLegalDisputeCountLessThanEqual(int count);

    // 用于行业健康状况

    /**
     * 获取公司数量最多的前15个行业的名称列表。
     * 使用原生SQL查询实现。
     * @return 行业名称列表
     */
    @Query(value = "SELECT industry FROM company_info WHERE industry IS NOT NULL AND industry <> '' GROUP BY industry ORDER BY COUNT(id) DESC LIMIT 15", nativeQuery = true)
    List<String> findTop15IndustriesByCompanyCount();

    /**
     * 根据行业名称计算该行业的平均法律纠纷数。
     * @param industry 行业名称
     * @return 平均法律纠纷数（Double类型，可能为null）
     */
    @Query("SELECT AVG(c.legalDisputeCount) FROM CompanyInfo c WHERE c.industry = :industry")
    Double findAverageLegalDisputesByIndustry(@Param("industry") String industry);

    // 用于供应链风险

    /**
     * 根据行业关键字计算该行业的平均舆情数量。
     * @param industry 行业关键字
     * @return 平均舆情数量（Double类型，可能为null）
     */
    @Query("SELECT AVG(c.publicOpinionCount) FROM CompanyInfo c WHERE c.industry LIKE %:industry%")
    Double findAveragePublicOpinionByIndustry(@Param("industry") String industry);

    /**
     * 统计指定行业中的财务风险公司数量（营收或利润为0或为空）。
     * @param industry 行业关键字
     * @return 财务风险公司数量
     */
    @Query("SELECT count(c) FROM CompanyInfo c WHERE (c.revenue = '0' OR c.profit = '0' OR c.revenue IS NULL OR c.profit IS NULL) AND c.industry LIKE %:industry%")
    long countFinancialRiskCompaniesByIndustry(@Param("industry") String industry);

    /**
     * 统计指定行业中的公司总数。
     * @param industry 行业关键字
     * @return 公司总数
     */
    @Query("SELECT count(c) FROM CompanyInfo c WHERE c.industry LIKE %:industry%")
    long countByIndustry(@Param("industry") String industry);

    // 用于风险分析

    /**
     * 查询高风险公司（法律纠纷数超过100，或营收/利润为0或为空）并分页返回。
     * @param pageable 分页参数
     * @return 高风险公司分页结果
     */
    @Query("SELECT c FROM CompanyInfo c WHERE c.legalDisputeCount > 100 OR c.revenue = '0' OR c.profit = '0' OR c.revenue IS NULL OR c.profit IS NULL")
    Page<CompanyInfo> findHighRiskCompanies(Pageable pageable);

    // 用于风险地图

    /**
     * 查询所有具有经纬度坐标的公司信息。
     * @return 具有坐标的公司列表
     */
    @Query("SELECT c FROM CompanyInfo c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<CompanyInfo> findAllWithCoordinates();

    // 用于供应链搜索

    /**
     * 根据公司名称或行业名称进行模糊查询并分页返回结果。
     * 忽略大小写。
     * @param name 公司名称关键词
     * @param industry 行业名称关键词
     * @param pageable 分页参数
     * @return 匹配的公司信息分页结果
     */
    Page<CompanyInfo> findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(String name, String industry, Pageable pageable);

    /**
     * 根据公司名称进行模糊查询，用于知识图谱的搜索功能。
     * @param name 公司名称关键词
     * @return 匹配的公司信息列表
     */
    List<CompanyInfo> findByNameContainingIgnoreCase(String name);

    /**
     * 根据公司名称进行模糊查询，并分页返回结果。
     * @param name 公司名称关键词
     * @param pageable 分页参数
     * @return 匹配的公司信息分页结果
     */
    Page<CompanyInfo> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
