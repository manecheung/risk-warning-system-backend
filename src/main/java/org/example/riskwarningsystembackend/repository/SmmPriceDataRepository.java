package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.dto.PriceDataDto;
import org.example.riskwarningsystembackend.entity.SmmPriceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SmmPriceDataRepository extends JpaRepository<SmmPriceData, Long> {

    /**
     * 检查特定指标在特定日期的数据是否存在
     *
     * @param indicatorId 指标ID
     * @param priceDate   价格日期
     * @return 如果存在则返回true，否则返回false
     */
    boolean existsByIndicatorIdAndPriceDate(Long indicatorId, LocalDate priceDate);

    /**
     * 根据指标ID和日期范围查询价格数据
     *
     * @param indicatorId 指标ID
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 价格数据列表
     */
    @Query("SELECT new org.example.riskwarningsystembackend.dto.PriceDataDto(spd.priceDate, spd.value) " +
           "FROM SmmPriceData spd " +
           "WHERE spd.indicator.id = :indicatorId " +
           "AND spd.priceDate >= :startDate AND spd.priceDate <= :endDate " +
           "ORDER BY spd.priceDate ASC")
    List<PriceDataDto> findPricesByIndicatorIdAndDateRange(@Param("indicatorId") Long indicatorId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
}
