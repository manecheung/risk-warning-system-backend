package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.dto.simulation.SimulationDTO;
import org.example.riskwarningsystembackend.dto.simulation.SimulationInfoDTO;
import org.example.riskwarningsystembackend.entity.Simulation;
import org.example.riskwarningsystembackend.repository.SimulationRepository;
import org.example.riskwarningsystembackend.service.SimulationQueryService;
import org.example.riskwarningsystembackend.service.SimulationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模拟场景控制器，用于处理与模拟场景相关的HTTP请求。
 * 提供创建、删除、查询模拟场景及其数据的功能。
 */
@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;
    private final SimulationQueryService simulationQueryService;
    private final SimulationRepository simulationRepository;

    /**
     * 构造方法，注入所需的Service和Repository依赖。
     *
     * @param simulationService      模拟场景业务逻辑服务
     * @param simulationQueryService 模拟场景查询服务
     * @param simulationRepository   模拟场景数据访问仓库
     */
    public SimulationController(SimulationService simulationService, SimulationQueryService simulationQueryService, SimulationRepository simulationRepository) {
        this.simulationService = simulationService;
        this.simulationQueryService = simulationQueryService;
        this.simulationRepository = simulationRepository;
    }

    /**
     * 创建一个新的模拟场景。
     * 接收名称、可选描述和上传的文件，调用服务进行处理。
     *
     * @param name        模拟场景名称
     * @param description 模拟场景描述（可选）
     * @param file        上传的文件内容
     * @return 成功时返回创建的Simulation对象，失败时返回错误信息
     */
    @PostMapping
    public ResponseEntity<?> createSimulation(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty."));
        }
        try {
            Simulation simulation = simulationService.createSimulation(name, description, file.getInputStream());
            // 返回创建的模拟场景的详细信息
            SimulationInfoDTO dto = new SimulationInfoDTO(simulation.getId(), simulation.getName(), simulation.getDescription(), simulation.getCreatedAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据ID删除指定的模拟场景。
     *
     * @param id 要删除的模拟场景ID
     * @return 成功时返回204状态码，失败时返回404及错误信息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSimulation(@PathVariable Long id) {
        try {
            simulationService.deleteSimulation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取所有模拟场景列表。
     *
     * @return 所有模拟场景的列表
     */
    @GetMapping
    public ResponseEntity<List<SimulationInfoDTO>> getAllSimulations() {
        List<SimulationInfoDTO> simulations = simulationRepository.findAll().stream()
                .map(sim -> new SimulationInfoDTO(sim.getId(), sim.getName(), sim.getDescription(), sim.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(simulations);
    }

    /**
     * 获取指定模拟场景的拓扑结构信息。
     *
     * @param id 模拟场景ID
     * @return 成功时返回拓扑结构数据，失败时返回404及错误信息
     */
    @GetMapping("/{id}/graph/topology")
    public ResponseEntity<?> getTopology(@PathVariable Long id) {
        try {
            SimulationDTO.TopologyResponse topology = simulationQueryService.getTopology(id);
            return ResponseEntity.ok(topology);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取指定模拟场景在特定时间步的数据。
     *
     * @param id   模拟场景ID
     * @param time 时间步索引
     */
    @GetMapping("/{id}/step/{time}")
    public ResponseEntity<?> getStepData(@PathVariable Long id, @PathVariable int time) {
        try {
            SimulationDTO.StepResponse stepData = simulationQueryService.getStepData(id, time);
            return ResponseEntity.ok(stepData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取指定模拟在特定时间步特定公司的详细数据。
     *
     * @param id        模拟场景ID
     * @param time      时间步索引
     * @param companyId 公司ID
     * @return 成功时返回公司的详细数据，失败时返回404及错误信息
     */
    @GetMapping("/{id}/step/{time}/company/{companyId}")
    public ResponseEntity<?> getCompanyDetails(
            @PathVariable Long id,
            @PathVariable int time,
            @PathVariable Integer companyId) {
        try {
            Map<String, Object> companyData = simulationQueryService.getCompanyDetailsForStep(id, time, companyId);
            return ResponseEntity.ok(companyData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
