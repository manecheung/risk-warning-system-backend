package org.example.riskwarningsystembackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.riskwarningsystembackend.dto.simulation.SimulationDTO;
import org.example.riskwarningsystembackend.entity.CompanySimulation.CompanySimulationData;
import org.example.riskwarningsystembackend.exception.ResourceNotFoundException;
import org.example.riskwarningsystembackend.repository.NodeStateProjection;
import org.example.riskwarningsystembackend.repository.SimulationCompanyDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模拟数据查询服务类，提供拓扑结构、时间步数据和公司详情的查询功能。
 */
@Service
public class SimulationQueryService {

    private final SimulationCompanyDataRepository dataRepository;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数，注入所需的依赖。
     *
     * @param dataRepository 模拟数据访问仓库
     * @param objectMapper   JSON对象映射器
     */
    public SimulationQueryService(SimulationCompanyDataRepository dataRepository, ObjectMapper objectMapper) {
        this.dataRepository = dataRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取指定模拟的拓扑结构数据（节点、边、时间范围）。
     * 优先使用 time=0 的数据构建静态拓扑以提升性能。
     * 如果没有 time=0 的数据，则加载整个模拟数据以确保健壮性。
     *
     * @param simulationId 模拟ID
     * @return 包含节点、边和时间范围的拓扑响应对象
     * @throws ResourceNotFoundException 当找不到对应模拟数据时抛出
     */
    @Transactional(readOnly = true)
    public SimulationDTO.TopologyResponse getTopology(Long simulationId) {
        // 优化：只取time=0的数据来构建静态拓扑，避免加载所有时间步的数据
        List<CompanySimulationData> topologyData = dataRepository.findBySimulationIdAndTime(simulationId, 0);
        if (topologyData.isEmpty()) {
            // 如果time=0没数据，尝试获取整个模拟的数据，以确保健壮性
            topologyData = dataRepository.findBySimulationId(simulationId);
            if (topologyData.isEmpty()) {
                throw new ResourceNotFoundException("No data found for simulation ID: " + simulationId);
            }
        }

        // 构建节点列表：从数据中提取公司ID与名称的映射，并转换为Node对象
        Map<Integer, String> companyIdToNameMap = topologyData.stream()
                .collect(Collectors.toMap(
                        CompanySimulationData::getCompanyId,
                        CompanySimulationData::getName,
                        (existing, replacement) -> existing // In case of duplicates, keep existing
                ));
        List<SimulationDTO.Node> nodes = companyIdToNameMap.entrySet().stream()
                .map(entry -> new SimulationDTO.Node(String.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());

        // 构建边列表：解析供应商和竞争对手关系，生成Edge对象并去重
        List<SimulationDTO.Edge> edges = new ArrayList<>();
        for (CompanySimulationData data : topologyData) {
            String sourceId = String.valueOf(data.getCompanyId());
            // Supplier edges
            if (data.getMaterials() != null) {
                data.getMaterials().forEach(material -> {
                    if (material.getWithSupplier() != null) {
                        material.getWithSupplier().forEach(supplierId -> edges.add(new SimulationDTO.Edge(String.valueOf(supplierId), sourceId, "supply")));
                    }
                });
            }
            // Competitor edges
            if (data.getProducts() != null) {
                data.getProducts().forEach(product -> {
                    if (product.getWithCompetitors() != null) {
                        product.getWithCompetitors().forEach(competitorId -> edges.add(new SimulationDTO.Edge(sourceId, String.valueOf(competitorId), "competition")));
                    }
                });
            }
        }
        List<SimulationDTO.Edge> distinctEdges = edges.stream().distinct().collect(Collectors.toList());


        // 获取该模拟的时间范围
        int minTime = dataRepository.findMinTimeBySimulationId(simulationId).orElse(0);
        int maxTime = dataRepository.findMaxTimeBySimulationId(simulationId).orElse(0);
        SimulationDTO.TimeRange timeRange = new SimulationDTO.TimeRange(minTime, maxTime);

        return new SimulationDTO.TopologyResponse(nodes, distinctEdges, timeRange);
    }

    /**
     * 获取指定模拟在特定时间步的节点状态数据。
     *
     * @param simulationId 模拟ID
     * @param time         时间步
     * @return 包含当前时间步节点状态的响应对象
     */
    @Transactional(readOnly = true)
    public SimulationDTO.StepResponse getStepData(Long simulationId, int time) {
        List<NodeStateProjection> projections = dataRepository.findNodeStatesBySimulationIdAndTime(simulationId, time);

        List<SimulationDTO.NodeState> nodesState = projections.stream()
                .map(p -> new SimulationDTO.NodeState(
                        String.valueOf(p.getCompanyId()),
                        p.getState(),
                        p.getKriScore() != null ? p.getKriScore() : 0.0,
                        p.getInnerFactor() != null ? p.getInnerFactor() : 0.0
                ))
                .collect(Collectors.toList());

        return new SimulationDTO.StepResponse(time, nodesState);
    }

    /**
     * 获取指定模拟中某公司在特定时间步的详细信息。
     *
     * @param simulationId 模拟ID
     * @param time         时间步
     * @param companyId    公司ID
     * @return 公司详细信息的Map表示
     * @throws ResourceNotFoundException 当找不到对应记录时抛出
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCompanyDetailsForStep(Long simulationId, int time, Integer companyId) {
        CompanySimulationData data = dataRepository.findBySimulationIdAndTimeAndCompanyId(simulationId, time, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No data found for simulation ID %d, time %d, and company ID %d",
                                simulationId, time, companyId)
                ));
        return objectMapper.convertValue(data, Map.class);
    }
}
