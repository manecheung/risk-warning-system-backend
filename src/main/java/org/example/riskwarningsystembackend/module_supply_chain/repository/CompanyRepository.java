package org.example.riskwarningsystembackend.module_supply_chain.repository;

import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 供应链企业数据访问接口
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 根据企业名称或行业模糊查询（分页）
     * @param name 企业名称关键词
     * @param industry 行业关键词
     * @param pageable 分页参数
     * @return 企业分页数据
     */
    Page<Company> findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(String name, String industry, Pageable pageable);

    /**
     * 统计不同行业的数量
     * @return 行业总数
     */
    @Query("SELECT COUNT(DISTINCT c.industry) FROM Company c")
    long countDistinctIndustry();

    /**
     * 根据公司名称查找公司实体。
     * Spring Data JPA 会根据方法名自动生成查询。
     * @param name 公司名称
     * @return 包含公司实体的 Optional，如果找不到则为空
     */
    Optional<Company> findByName(String name);
}

