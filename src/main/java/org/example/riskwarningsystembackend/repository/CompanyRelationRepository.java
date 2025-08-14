package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface CompanyRelationRepository extends JpaRepository<CompanyRelation, Long> {

    List<CompanyRelation> findByRelationType(String relationType);

    /**
     * 根据单个公司ID查找所有相关的关系。
     * 无论是作为关系的一方（companyOneId）还是另一方（companyTwoId），
     * 只要包含了指定的companyId，都会被查询出来。
     * 这是实现图谱按需展开功能的核心。
     * @param companyId 要查询的公司ID
     * @return 该公司的所有直接关系列表
     */
    @Query("SELECT r FROM CompanyRelation r WHERE r.companyOneId = :companyId OR r.companyTwoId = :companyId")
    List<CompanyRelation> findAllByCompanyId(@Param("companyId") Long companyId);

    /**
     * 根据一组公司ID，查询出所有与这些公司相关的关系。
     * 只要一个关系的任意一方存在于给定的 companyIds 集合中，这个关系就会被返回。
     * 这个方法用于高效地构建初始图谱和搜索子图，避免了在内存中全量加载和过滤。
     * @param companyIds 一组公司的ID集合
     * @return 所有相关的关系列表
     */
    @Query("SELECT r FROM CompanyRelation r WHERE r.companyOneId IN :companyIds OR r.companyTwoId IN :companyIds")
    List<CompanyRelation> findAllByCompanyIds(@Param("companyIds") Set<Long> companyIds);
}