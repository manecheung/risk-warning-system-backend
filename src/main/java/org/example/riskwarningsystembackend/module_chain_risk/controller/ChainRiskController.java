package org.example.riskwarningsystembackend.module_chain_risk.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.common.dto.Result;
import org.example.riskwarningsystembackend.module_chain_risk.entity.Simulation;
import org.example.riskwarningsystembackend.module_chain_risk.service.ChainRiskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 产业链风险预警模块控制器
 */
@RestController
@RequestMapping("/chain-risk")
@RequiredArgsConstructor
public class ChainRiskController {

    private final ChainRiskService chainRiskService;

    /**
     * 3.7 获取产业链知识图谱数据
     * (暂时保留Mock，后续可由Service实现)
     */
    @GetMapping("/graph")
    public ResponseEntity<Result<Map<String, Object>>> getGraph(@RequestParam(required = false) String companyId) {
        return ResponseEntity.ok(Result.success(chainRiskService.getGraph(companyId)));
    }

    /**
     * 5.1 获取风险蔓延模拟列表
     */
    @GetMapping("/simulations")
    public ResponseEntity<Result<Map<String, Object>>> getSimulations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Simulation> simPage = chainRiskService.getSimulations(pageable, keyword);
        Map<String, Object> response = Map.of(
                "page", simPage.getNumber() + 1,
                "pageSize", simPage.getSize(),
                "totalRecords", simPage.getTotalElements(),
                "totalPages", simPage.getTotalPages(),
                "hasPrevPage", simPage.hasPrevious(),
                "hasNextPage", simPage.hasNext(),
                "records", simPage.getContent()
        );
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 5.2 导入并保存风险蔓延模拟数据
     */
    @PostMapping("/simulations")
    public ResponseEntity<Result<Map<String, Long>>> createSimulation(@RequestBody Map<String, Object> payload) throws JsonProcessingException {
        Simulation newSim = chainRiskService.createSimulation(payload);
        return new ResponseEntity<>(Result.success(Map.of("id", newSim.getId()), "创建成功"), HttpStatus.CREATED);
    }

    /**
     * 5.3 启动风险蔓延模拟
     */
    @PostMapping("/simulations/{id}/run")
    public ResponseEntity<Result<Map<String, Object>>> runSimulation(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String startNodeId = payload.get("startNodeId");
        Map<String, Object> result = chainRiskService.runSimulation(id, startNodeId);
        return ResponseEntity.ok(Result.success(result, "模拟成功"));
    }

    /**
     * 5.4 删除风险蔓延模拟场景
     */
    @DeleteMapping("/simulations/{id}")
    public ResponseEntity<Result<Void>> deleteSimulation(@PathVariable Long id) {
        chainRiskService.deleteSimulation(id);
        return ResponseEntity.ok(Result.success(null, "删除成功"));
    }
}
