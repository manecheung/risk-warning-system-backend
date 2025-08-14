package org.example.riskwarningsystembackend.controller;

import jakarta.validation.Valid;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.SimulationDTO;
import org.example.riskwarningsystembackend.entity.ChainRiskSimulation;
import org.example.riskwarningsystembackend.service.ChainRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chain-risk/simulations")
public class ChainRiskController {

    @Autowired
    private ChainRiskService chainRiskService;

    @GetMapping
    public RestResult<PaginatedResponseDTO<ChainRiskSimulation>> getSimulations(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword) {
        Page<ChainRiskSimulation> resultPage = chainRiskService.getSimulations(page, pageSize, keyword);
        return RestResult.success(new PaginatedResponseDTO<>(resultPage));
    }

    @PostMapping
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

    @DeleteMapping("/{id}")
    public RestResult<Void> deleteSimulation(@PathVariable Long id) {
        chainRiskService.deleteSimulation(id);
        return RestResult.success(null);
    }

    @PostMapping("/{id}/run")
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
