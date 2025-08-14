package org.example.riskwarningsystembackend.controller;

import jakarta.validation.Valid;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.SimulationDTO;
import org.example.riskwarningsystembackend.entity.ChainRiskSimulation;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.service.ChainRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chain-risk")
public class ChainRiskController {

    @Autowired
    private ChainRiskService chainRiskService;
    @Autowired
    private CompanyInfoRepository companyInfoRepository;

    @PostMapping("/run-new")
    public RestResult<?> runNewSimulation(
            @RequestBody Map<String, String> payload) {
        String startNodeName = payload.get("startNodeName");
        if (startNodeName == null) {
            return RestResult.failure(ResultCode.BAD_REQUEST, "startNodeName is required");
        }

        Optional<CompanyInfo> companyOptional = companyInfoRepository.findByName(startNodeName);
        if (companyOptional.isEmpty()) {
            return RestResult.failure(ResultCode.NOT_FOUND, "Company with name '" + startNodeName + "' not found.");
        }

        String startNodeId = companyOptional.get().getId().toString();

        // Default values for simulation parameters
        double initialRisk = 1.0;
        double decayRate = 0.5;
        int maxLevel = 5;

        ChainRiskService.RiskPropagationDTO result = chainRiskService.runNewSimulation(startNodeId, initialRisk, decayRate, maxLevel);
        return RestResult.success(result);
    }

    @GetMapping("/simulations")
    public RestResult<PaginatedResponseDTO<ChainRiskSimulation>> getSimulations(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword) {
        Page<ChainRiskSimulation> resultPage = chainRiskService.getSimulations(page, pageSize, keyword);
        return RestResult.success(new PaginatedResponseDTO<>(resultPage));
    }

    @PostMapping("/simulations")
    public ResponseEntity<RestResult<Map<String, Long>>> saveSimulation(@Valid @RequestBody SimulationDTO simulationDto) {
        try {
            // In a real app, get the creator name from Spring Security Principal
            String creator = "分析小组";
            ChainRiskSimulation savedSimulation = chainRiskService.saveSimulation(simulationDto, creator);
            return new ResponseEntity<>(RestResult.success(Map.of("id", savedSimulation.getId())), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(RestResult.failure(ResultCode.BAD_REQUEST, "Failed to process simulation data"), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/simulations/{id}")
    public RestResult<Void> deleteSimulation(@PathVariable Long id) {
        chainRiskService.deleteSimulation(id);
        return RestResult.success(null);
    }

    @PostMapping("/simulations/{id}/run")
    public RestResult<Map<String, Object>> runSimulation(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload) throws IOException {
        String startNodeId = payload.get("startNodeId");
        if (startNodeId == null) {
            return RestResult.failure(ResultCode.BAD_REQUEST, "startNodeId is required");
        }
        Map<String, Object> result = chainRiskService.runSimulation(id, startNodeId);
        return RestResult.success(result);
    }
}
