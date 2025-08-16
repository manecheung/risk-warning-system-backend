package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.CompanySimulation.CompanySimulationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 仿真公司数据仓库接口
 * 提供对公司仿真数据的数据库操作方法
 */
@Repository
public interface SimulationCompanyDataRepository extends JpaRepository<CompanySimulationData, Long> {

    /**
     * 根据仿真ID查找所有相关的公司仿真数据
     *
     * @param simulationId 仿真ID
     * @return 公司仿真数据列表
     */
    List<CompanySimulationData> findBySimulationId(Long simulationId);

    /**
     * 根据仿真ID和时间查找公司仿真数据
     *
     * @param simulationId 仿真ID
     * @param time 时间点
     * @return 公司仿真数据列表
     */
    List<CompanySimulationData> findBySimulationIdAndTime(Long simulationId, int time);

    /**
     * 根据仿真ID、时间和公司ID查找特定的公司仿真数据
     *
     * @param simulationId 仿真ID
     * @param time 时间点
     * @param companyId 公司ID
     * @return 包含公司仿真数据的Optional对象
     */
    Optional<CompanySimulationData> findBySimulationIdAndTimeAndCompanyId(Long simulationId, int time, Integer companyId);

    /**
     * 根据仿真ID和时间查询节点状态信息
     * 查询公司ID、状态、KRI分数和内部因子等投影数据
     *
     * @param simulationId 仿真ID
     * @param time 时间点
     * @return 节点状态投影数据列表
     */
    @Query("SELECT csd.companyId as companyId, csd.state as state, csd.kris.kriScore as kriScore, csd.innerFactor as innerFactor FROM CompanySimulationData csd WHERE csd.simulation.id = :simulationId AND csd.time = :time")
    List<NodeStateProjection> findNodeStatesBySimulationIdAndTime(@Param("simulationId") Long simulationId, @Param("time") int time);

    /**
     * 查询指定仿真ID的最小时间值
     *
     * @param simulationId 仿真ID
     * @return 包含最小时间值的Optional对象
     */
    @Query("SELECT MIN(csd.time) FROM CompanySimulationData csd WHERE csd.simulation.id = :simulationId")
    Optional<Integer> findMinTimeBySimulationId(@Param("simulationId") Long simulationId);

    /**
     * 查询指定仿真ID的最大时间值
     *
     * @param simulationId 仿真ID
     * @return 包含最大时间值的Optional对象
     */
    @Query("SELECT MAX(csd.time) FROM CompanySimulationData csd WHERE csd.simulation.id = :simulationId")
    Optional<Integer> findMaxTimeBySimulationId(@Param("simulationId") Long simulationId);
}

