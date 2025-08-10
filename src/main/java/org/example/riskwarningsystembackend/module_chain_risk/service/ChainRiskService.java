package org.example.riskwarningsystembackend.module_chain_risk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_chain_risk.entity.CompanyRelationship;
import org.example.riskwarningsystembackend.module_chain_risk.entity.Simulation;
import org.example.riskwarningsystembackend.module_chain_risk.repository.CompanyRelationshipRepository;
import org.example.riskwarningsystembackend.module_chain_risk.repository.SimulationRepository;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChainRiskService {

    private final SimulationRepository simulationRepository;
    private final CompanyRepository companyRepository;
    private final CompanyRelationshipRepository relationshipRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Map<String, Object> getGraph(String companyId) {
        List<Company> companies = companyRepository.findAll();
        List<CompanyRelationship> relationships = relationshipRepository.findAll();

        List<Map<String, Object>> nodes = companies.stream().map(c -> {
            Map<String, Object> node = new java.util.HashMap<>();
            node.put("id", "company-" + c.getId());
            node.put("label", c.getName());
            node.put("size", 40);
            return node;
        }).collect(Collectors.toList());

        List<Map<String, Object>> edges = relationships.stream().map(r -> {
            Map<String, Object> edge = new java.util.HashMap<>();
            edge.put("source", "company-" + r.getSource().getId());
            edge.put("target", "company-" + r.getTarget().getId());
            edge.put("label", r.getLabel());
            edge.put("type", r.getType());
            return edge;
        }).collect(Collectors.toList());

        return Map.of("nodes", nodes, "edges", edges);
    }

    @Transactional(readOnly = true)
    public Page<Simulation> getSimulations(Pageable pageable, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return simulationRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        return simulationRepository.findAll(pageable);
    }

    public Simulation createSimulation(Map<String, Object> payload) throws JsonProcessingException {
        Simulation sim = new Simulation();
        sim.setName((String) payload.get("name"));
        sim.setDescription((String) payload.get("description"));
        sim.setCreator("分析师"); // 实际应为当前登录用户
        sim.setCreateTime(LocalDateTime.now());
        sim.setNodesJson(objectMapper.writeValueAsString(payload.get("nodes")));
        sim.setEdgesJson(objectMapper.writeValueAsString(payload.get("edges")));
        sim.setRiskPathJson(objectMapper.writeValueAsString(payload.get("riskPath")));
        return simulationRepository.save(sim);
    }

    public void deleteSimulation(Long id) {
        simulationRepository.deleteById(id);
    }

    /**
     * 运行风险蔓延模拟
     * @param id 模拟场景ID
     * @param startNodeId 风险起始节点ID
     * @return 包含模拟结果的Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> runSimulation(Long id, String startNodeId) {
        Simulation sim = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的模拟场景"));

        try {
            // 1. 解析图数据
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {};
            List<Map<String, Object>> nodes = objectMapper.readValue(sim.getNodesJson(), typeRef);
            List<Map<String, Object>> edges = objectMapper.readValue(sim.getEdgesJson(), typeRef);

            // 2. 构建邻接表以进行图遍历
            Map<String, List<String>> adjList = new HashMap<>();
            for (Map<String, Object> edge : edges) {
                String source = (String) edge.get("source");
                String target = (String) edge.get("target");
                adjList.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
            }

            // 3. 执行广度优先搜索 (BFS) 来确定风险路径
            List<List<String>> riskPath = new ArrayList<>();
            Queue<String> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();

            queue.add(startNodeId);
            visited.add(startNodeId);

            while (!queue.isEmpty()) {
                int levelSize = queue.size();
                List<String> currentLevelNodes = new ArrayList<>();
                for (int i = 0; i < levelSize; i++) {
                    String u = queue.poll();
                    // 不将风险源本身加入路径
                    if (!u.equals(startNodeId)) {
                        currentLevelNodes.add(u);
                    }

                    if (adjList.containsKey(u)) {
                        for (String v : adjList.get(u)) {
                            if (!visited.contains(v)) {
                                visited.add(v);
                                queue.add(v);
                            }
                        }
                    }
                }
                if (!currentLevelNodes.isEmpty()) {
                    riskPath.add(currentLevelNodes);
                }
            }

            // 4. 组装并返回API规约的格式
            Map<String, Object> result = new HashMap<>();
            result.put("simulationId", sim.getId());
            result.put("simulationName", sim.getName());
            result.put("nodes", nodes);
            result.put("edges", edges);
            result.put("riskPath", riskPath);

            return result;

        } catch (IOException e) {
            // 在生产环境中，应使用更具体的异常处理和日志记录
            throw new RuntimeException("解析模拟数据失败", e);
        }
    }
}