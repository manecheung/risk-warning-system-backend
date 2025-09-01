package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.SmmIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmmIndicatorRepository extends JpaRepository<SmmIndicator, Long> {

    /**
     * 根据SMM的指标ID查找指标信息
     *
     * @param quotaId SMM指标ID
     * @return 指标信息
     */
    Optional<SmmIndicator> findByQuotaId(String quotaId);

    /**
     * 根据更新频率查找指标列表
     *
     * @param frequency 更新频率 (例如 "日", "周")
     * @return 符合条件的指标列表
     */
    List<SmmIndicator> findByFrequency(String frequency);
}
