package org.example.riskwarningsystembackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.riskwarningsystembackend.dto.SimulationDTO;
import org.example.riskwarningsystembackend.entity.ChainRiskSimulation;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.exception.ResourceNotFoundException;
import org.example.riskwarningsystembackend.repository.ChainRiskSimulationRepository;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.CompanyRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChainRiskService {

    @Autowired
    private ChainRiskSimulationRepository simulationRepository;
    @Autowired
    private CompanyInfoRepository companyInfoRepository;
    @Autowired
    private CompanyRelationRepository companyRelationRepository;

    @Autowired
    private ObjectMapper objectMapper; // Jackson's ObjectMapper for JSON processing

    // DTO for Risk Propagation Simulation Result
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskPropagationDTO {
        private List<RiskNode> nodes;
        private List<RiskEdge> edges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskNode {
        private String id;
        private String label;
        private double riskValue;
        private int level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskEdge {
        private String source;
        private String target;
        private String label;
    }


    @Transactional(readOnly = true)
    public RiskPropagationDTO runNewSimulation(String startCompanyId, double initialRisk, double decayRate, int maxLevel) {
        // 1. Fetch all companies and relations
        Map<Long, CompanyInfo> companyMap = companyInfoRepository.findAll().stream()
                .collect(Collectors.toMap(CompanyInfo::getId, Function.identity()));

        if (!companyMap.containsKey(Long.parseLong(startCompanyId))) {
            throw new ResourceNotFoundException("Start company with id " + startCompanyId + " not found.");
        }

        List<CompanyRelation> relations = companyRelationRepository.findByRelationType("partner");

        // 2. Build adjacency list for graph traversal
        Map<Long, List<Long>> adjList = new HashMap<>();
        for (CompanyRelation relation : relations) {
            adjList.computeIfAbsent(relation.getCompanyOneId(), k -> new ArrayList<>()).add(relation.getCompanyTwoId());
            adjList.computeIfAbsent(relation.getCompanyTwoId(), k -> new ArrayList<>()).add(relation.getCompanyOneId());
        }

        // 3. BFS Simulation Logic
        Map<Long, RiskNode> affectedNodes = new HashMap<>();
        Set<String> affectedEdges = new HashSet<>();

        Queue<RiskNode> queue = new LinkedList<>();

        // Initial node
        CompanyInfo startCompany = companyMap.get(Long.parseLong(startCompanyId));
        RiskNode startNode = new RiskNode(startCompanyId, startCompany.getName(), initialRisk, 0);
        queue.add(startNode);
        affectedNodes.put(Long.parseLong(startCompanyId), startNode);

        while (!queue.isEmpty()) {
            RiskNode currentNode = queue.poll();

            if (currentNode.getLevel() >= maxLevel) {
                continue;
            }

            Long currentId = Long.parseLong(currentNode.getId());
            if (adjList.containsKey(currentId)) {
                for (Long neighborId : adjList.get(currentId)) {
                    double newRisk = currentNode.getRiskValue() * decayRate;

                    if (newRisk < 0.01) continue; // Stop propagation if risk is negligible

                    int newLevel = currentNode.getLevel() + 1;

                    if (!affectedNodes.containsKey(neighborId) || affectedNodes.get(neighborId).getRiskValue() < newRisk) {
                        CompanyInfo neighborCompany = companyMap.get(neighborId);
                        if (neighborCompany != null) {
                            RiskNode neighborNode = new RiskNode(
                                    String.valueOf(neighborId),
                                    neighborCompany.getName(),
                                    newRisk,
                                    newLevel
                            );
                            affectedNodes.put(neighborId, neighborNode);
                            queue.add(neighborNode);
                        }
                    }
                    // Add edge to result
                    String edgeKey = currentId < neighborId ? currentId + "-" + neighborId : neighborId + "-" + currentId;
                    affectedEdges.add(edgeKey);
                }
            }
        }

        // 4. Prepare response DTO
        List<RiskEdge> finalEdges = affectedEdges.stream().map(edgeKey -> {
            String[] ids = edgeKey.split("-");
            return new RiskEdge(ids[0], ids[1], "合作");
        }).collect(Collectors.toList());

        return new RiskPropagationDTO(new ArrayList<>(affectedNodes.values()), finalEdges);
    }


    @Transactional(readOnly = true)
    public Page<ChainRiskSimulation> getSimulations(int page, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        if (!StringUtils.hasText(keyword)) {
            return simulationRepository.findAll(pageable);
        }

        Specification<ChainRiskSimulation> spec = (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };

        return simulationRepository.findAll(spec, pageable);
    }

    @Transactional
    public ChainRiskSimulation saveSimulation(SimulationDTO dto, String creator) throws JsonProcessingException {
        ChainRiskSimulation simulation = new ChainRiskSimulation();
        simulation.setName(dto.getName());
        simulation.setDescription(dto.getDescription());
        simulation.setCreator(creator); // Set creator from authenticated user

        // Serialize graph data to JSON strings
        simulation.setNodes(objectMapper.writeValueAsString(dto.getNodes()));
        simulation.setEdges(objectMapper.writeValueAsString(dto.getEdges()));
        if (dto.getRiskPath() != null) {
            simulation.setRiskPath(objectMapper.writeValueAsString(dto.getRiskPath()));
        }

        return simulationRepository.save(simulation);
    }

    @Transactional
    public void deleteSimulation(Long id) {
        if (!simulationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Simulation not found with id: " + id);
        }
        simulationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> runSimulation(Long id, String startNodeId) throws IOException {
        ChainRiskSimulation simulation = simulationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Simulation not found with id: " + id));

        // Deserialize nodes and edges
        TypeReference<List<Map<String, Object>>> mapTypeRef = new TypeReference<>() {};
        List<Map<String, Object>> nodes = objectMapper.readValue(simulation.getNodes(), mapTypeRef);
        List<Map<String, Object>> edges = objectMapper.readValue(simulation.getEdges(), mapTypeRef);

        // Build adjacency list for graph traversal
        Map<String, List<String>> adjList = new HashMap<>();
        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");
            adjList.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
        }

        // --- BFS Simulation Logic ---
        List<List<String>> riskPath = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        if (nodes.stream().noneMatch(n -> n.get("id").equals(startNodeId))) {
             throw new IllegalArgumentException("Start node with id " + startNodeId + " not found in this simulation.");
        }

        queue.add(startNodeId);
        visited.add(startNodeId);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<String> currentLevelNodes = new ArrayList<>();
            for (int i = 0; i < levelSize; i++) {
                String u = queue.poll();
                if (!startNodeId.equals(u)) { // Safer equals check
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

        // Prepare response object as defined in gemini.md
        Map<String, Object> response = new HashMap<>();
        response.put("simulationId", simulation.getId());
        response.put("simulationName", simulation.getName());
        response.put("nodes", objectMapper.readValue(simulation.getNodes(), new TypeReference<List<JsonNode>>() {}));
        response.put("edges", objectMapper.readValue(simulation.getEdges(), new TypeReference<List<JsonNode>>() {}));
        response.put("riskPath", riskPath);

        return response;
    }
}

