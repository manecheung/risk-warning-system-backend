package org.example.riskwarningsystembackend.chainrisk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 产业链风险控制器类
 * 处理产业链风险模拟相关的REST API请求
 */
@RestController
@RequestMapping("/api/v1/chain-risk")
public class ChainRiskController {

    private static final Logger logger = LoggerFactory.getLogger(ChainRiskController.class);

    private final ChainRiskService chainRiskService;

    @Autowired
    public ChainRiskController(ChainRiskService chainRiskService) {
        this.chainRiskService = chainRiskService;
    }

    /**
     * 运行产业链风险模拟
     * 根据指定的初始异常节点运行模拟，并返回模拟结果
     *
     * @param request 包含初始异常节点信息的模拟请求对象
     * @return ResponseEntity<?> 包含模拟结果或错误信息的响应实体
     */
    @PostMapping("/run")
    public ResponseEntity<?> runSimulation(@RequestBody SimulationRequest request) {
        // 检查请求中的初始异常节点是否为空
        if (request.getInitialAbnormalNode() == null || request.getInitialAbnormalNode().isEmpty()) {
            return ResponseEntity.badRequest().body("initialAbnormalNode is required.");
        }

        try {
            // 执行模拟并返回结果
            SimulationResult result = chainRiskService.runSimulation(request.getInitialAbnormalNode());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("模拟请求参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            logger.error("模拟请求期间加载贸易数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load trade data.");
        }
    }
}
