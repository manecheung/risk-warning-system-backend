package org.example.riskwarningsystembackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.entity.ChainRiskSimulation;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.exception.ResourceNotFoundException;
import org.example.riskwarningsystembackend.repository.ChainRiskSimulationRepository;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.CompanyRelationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ChainRiskService {

    private final ChainRiskSimulationRepository simulationRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final CompanyRelationRepository companyRelationRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, Map.Entry<Long, SimulationResultDTO>> simulationCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleaner = Executors.newSingleThreadScheduledExecutor();
    private static final long CACHE_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(10);

    public ChainRiskService(ChainRiskSimulationRepository simulationRepository, CompanyInfoRepository companyInfoRepository, CompanyRelationRepository companyRelationRepository, ObjectMapper objectMapper) {
        cacheCleaner.scheduleAtFixedRate(this::cleanExpiredCacheEntries, 10, 10, TimeUnit.MINUTES);
        this.simulationRepository = simulationRepository;
        this.companyInfoRepository = companyInfoRepository;
        this.companyRelationRepository = companyRelationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public SimulationResultDTO runSimulationWithSteps(String startCompanyIdStr, double initialRisk, double decayRate, int maxLevel) {
        Long startCompanyId = Long.parseLong(startCompanyIdStr);
        Map<Long, CompanyInfo> companyMap = new HashMap<>();
        CompanyInfo startCompany = companyInfoRepository.findById(startCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("起始公司ID不存在: " + startCompanyId));
        companyMap.put(startCompanyId, startCompany);

        List<SimulationStepDTO> steps = new ArrayList<>();
        Map<Long, RiskNodeDTO> affectedNodes = new HashMap<>();
        Set<String> affectedEdges = new HashSet<>();
        Queue<RiskNodeDTO> queue = new LinkedList<>();

        RiskNodeDTO startNode = new RiskNodeDTO(startCompanyIdStr, startCompany.getName(), initialRisk, 0);
        queue.add(startNode);
        affectedNodes.put(startCompanyId, startNode);
        steps.add(new SimulationStepDTO(0, startCompanyIdStr, startCompany.getName(), initialRisk, 0, determineStatus(initialRisk), null));

        int currentLevel = 0;
        while (!queue.isEmpty()) {
            if (++currentLevel > maxLevel) break;
            Set<Long> currentLevelNodeIds = queue.stream().map(n -> Long.parseLong(n.getId())).collect(Collectors.toSet());
            if (currentLevelNodeIds.isEmpty()) break;
            int levelSize = queue.size();

            List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(currentLevelNodeIds);
            Map<Long, List<Long>> adjListForLevel = new HashMap<>();
            Set<Long> neighborIdsToFetch = new HashSet<>();
            for (CompanyRelation r : relations) {
                Long id1 = r.getCompanyOneId();
                Long id2 = r.getCompanyTwoId();
                adjListForLevel.computeIfAbsent(id1, k -> new ArrayList<>()).add(id2);
                adjListForLevel.computeIfAbsent(id2, k -> new ArrayList<>()).add(id1);
                if (currentLevelNodeIds.contains(id1)) neighborIdsToFetch.add(id2);
                if (currentLevelNodeIds.contains(id2)) neighborIdsToFetch.add(id1);
            }

            neighborIdsToFetch.removeAll(companyMap.keySet());
            if (!neighborIdsToFetch.isEmpty()) {
                companyInfoRepository.findAllById(neighborIdsToFetch).forEach(c -> companyMap.put(c.getId(), c));
            }

            for (int i = 0; i < levelSize; i++) {
                RiskNodeDTO currentNode = queue.poll();
                if (currentNode == null) {
                    continue;
                }
                Long currentId = Long.parseLong(currentNode.getId());
                if (!adjListForLevel.containsKey(currentId)) continue;

                for (Long neighborId : adjListForLevel.get(currentId)) {
                    double newRisk = currentNode.getRiskValue() * decayRate;
                    if (newRisk < 0.01) continue;
                    if (!affectedNodes.containsKey(neighborId) || affectedNodes.get(neighborId).getRiskValue() < newRisk) {
                        CompanyInfo neighborCompany = companyMap.get(neighborId);
                        if (neighborCompany != null) {
                            RiskNodeDTO neighborNode = new RiskNodeDTO(String.valueOf(neighborId), neighborCompany.getName(), newRisk, currentLevel);
                            affectedNodes.put(neighborId, neighborNode);
                            queue.add(neighborNode);
                            steps.add(new SimulationStepDTO(currentLevel, String.valueOf(neighborId), neighborCompany.getName(), newRisk, currentLevel, determineStatus(newRisk), String.valueOf(currentId)));
                        }
                    }
                    String edgeKey = currentId < neighborId ? currentId + "-" + neighborId : neighborId + "-" + currentId;
                    affectedEdges.add(edgeKey);
                }
            }
        }

        List<RiskEdgeDTO> finalEdges = affectedEdges.stream().map(edgeKey -> {
            String[] ids = edgeKey.split("-");
            return new RiskEdgeDTO(ids[0], ids[1], "合作");
        }).collect(Collectors.toList());

        String runId = UUID.randomUUID().toString();
        SimulationResultDTO result = new SimulationResultDTO(runId, steps, new ArrayList<>(affectedNodes.values()), finalEdges);
        simulationCache.put(runId, new AbstractMap.SimpleEntry<>(System.currentTimeMillis(), result));
        return result;
    }

    @Transactional(readOnly = true)
    public SimulationResultDTO getSimulationById(Long id) throws JsonProcessingException {
        ChainRiskSimulation simulation = simulationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("模拟记录不存在, id: " + id));

        List<RiskNodeDTO> finalNodes = objectMapper.readValue(simulation.getNodes(), new TypeReference<>() {});
        List<RiskEdgeDTO> finalEdges = objectMapper.readValue(simulation.getEdges(), new TypeReference<>() {});
        List<SimulationStepDTO> steps = objectMapper.readValue(simulation.getRiskPath(), new TypeReference<>() {});

        return new SimulationResultDTO(String.valueOf(id), steps, finalNodes, finalEdges);
    }

    @Transactional
    public ChainRiskSimulation commitSimulation(String runId, String name, String description, String creator) throws JsonProcessingException {
        Map.Entry<Long, SimulationResultDTO> cacheEntry = simulationCache.get(runId);
        if (cacheEntry == null) {
            throw new ResourceNotFoundException("模拟结果不存在或已过期, runId: " + runId);
        }
        SimulationResultDTO resultToSave = cacheEntry.getValue();

        ChainRiskSimulation simulation = new ChainRiskSimulation();
        simulation.setName(name);
        simulation.setDescription(description);
        simulation.setCreator(creator);
        simulation.setNodes(objectMapper.writeValueAsString(resultToSave.getFinalNodes()));
        simulation.setEdges(objectMapper.writeValueAsString(resultToSave.getFinalEdges()));
        simulation.setRiskPath(objectMapper.writeValueAsString(resultToSave.getSteps()));

        ChainRiskSimulation savedEntity = simulationRepository.save(simulation);
        simulationCache.remove(runId);
        return savedEntity;
    }

    @Transactional(readOnly = true)
    public Page<ChainRiskSimulation> getSimulations(int page, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        if (!StringUtils.hasText(keyword)) {
            return simulationRepository.findAll(pageable);
        }
        Specification<ChainRiskSimulation> spec = (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("name")), pattern), cb.like(cb.lower(root.get("description")), pattern));
        };
        return simulationRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteSimulation(Long id) {
        if (!simulationRepository.existsById(id)) {
            throw new ResourceNotFoundException("无法删除：模拟记录不存在, id: " + id);
        }
        simulationRepository.deleteById(id);
    }

    private String determineStatus(double riskValue) {
        if (riskValue > 0.7) return "CRITICAL";
        if (riskValue > 0.3) return "WARNING";
        return "NORMAL";
    }

    private void cleanExpiredCacheEntries() {
        long now = System.currentTimeMillis();
        simulationCache.entrySet().removeIf(entry -> (now - entry.getValue().getKey()) > CACHE_EXPIRATION_MS);
    }

    @PreDestroy
    public void onShutdown() {
        cacheCleaner.shutdown();
    }
}
