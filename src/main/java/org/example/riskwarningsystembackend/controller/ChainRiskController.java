package org.example.riskwarningsystembackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.CommitSimulationRequest;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.RunSimulationRequest;
import org.example.riskwarningsystembackend.dto.SimulationResultDTO;
import org.example.riskwarningsystembackend.entity.ChainRiskSimulation;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.exception.ResourceNotFoundException;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.service.ChainRiskService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chain-risk")
public class ChainRiskController {

    private final ChainRiskService chainRiskService;
    private final CompanyInfoRepository companyInfoRepository;

    public ChainRiskController(ChainRiskService chainRiskService, CompanyInfoRepository companyInfoRepository) {
        this.chainRiskService = chainRiskService;
        this.companyInfoRepository = companyInfoRepository;
    }

    @PostMapping("/run-new")
    public RestResult<SimulationResultDTO> runNewSimulation(@Valid @RequestBody RunSimulationRequest request) {
        Optional<CompanyInfo> companyOptional = companyInfoRepository.findByName(request.getStartNodeName());
        if (companyOptional.isEmpty()) {
            return RestResult.failure(ResultCode.NOT_FOUND, "公司名称不存在: '" + request.getStartNodeName() + "'");
        }
        String startNodeId = companyOptional.get().getId().toString();

        double initialRisk = Optional.ofNullable(request.getInitialRisk()).orElse(1.0);
        double decayRate = Optional.ofNullable(request.getDecayRate()).orElse(0.5);
        int maxLevel = Optional.ofNullable(request.getMaxLevel()).orElse(5);

        SimulationResultDTO result = chainRiskService.runSimulationWithSteps(startNodeId, initialRisk, decayRate, maxLevel);
        return RestResult.success(result);
    }

    @PostMapping("/simulations/commit")
    public ResponseEntity<RestResult<Map<String, Long>>> commitSimulation(
            @Valid @RequestBody CommitSimulationRequest request,
            Authentication authentication) {
        try {
            String creator;
            if (authentication != null && authentication.isAuthenticated()) {
                creator = authentication.getName();
            } else {
                creator = "匿名用户";
            }

            ChainRiskSimulation savedSimulation = chainRiskService.commitSimulation(
                request.getRunId(), request.getName(), request.getDescription(), creator);
            return new ResponseEntity<>(RestResult.success(Map.of("id", savedSimulation.getId())), HttpStatus.CREATED);
        } catch (ResourceNotFoundException | JsonProcessingException e) {
            return new ResponseEntity<>(RestResult.failure(ResultCode.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/simulations/{id}")
    public RestResult<SimulationResultDTO> getSimulationById(@PathVariable Long id) {
        try {
            SimulationResultDTO result = chainRiskService.getSimulationById(id);
            return RestResult.success(result);
        } catch (JsonProcessingException e) {
            return RestResult.failure(ResultCode.FAILURE, "解析模拟数据失败");
        }
    }

    @GetMapping("/simulations")
    public RestResult<PaginatedResponseDTO<ChainRiskSimulation>> getSimulations(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword) {
        Page<ChainRiskSimulation> resultPage = chainRiskService.getSimulations(page, pageSize, keyword);
        return RestResult.success(new PaginatedResponseDTO<>(resultPage));
    }

    @DeleteMapping("/simulations/{id}")
    public RestResult<Void> deleteSimulation(@PathVariable Long id) {
        chainRiskService.deleteSimulation(id);
        return RestResult.success(null);
    }
}