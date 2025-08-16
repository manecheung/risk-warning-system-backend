package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.entity.Simulation;
import org.example.riskwarningsystembackend.repository.SimulationRepository;
import org.example.riskwarningsystembackend.service.DataInitial.SimulationDataImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * 仿真服务类，提供仿真数据的创建和删除功能
 */
@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationDataImportService simulationDataImportService;

    public SimulationService(SimulationRepository simulationRepository, SimulationDataImportService simulationDataImportService) {
        this.simulationRepository = simulationRepository;
        this.simulationDataImportService = simulationDataImportService;
    }

    /**
     * 创建一个新的仿真记录
     *
     * @param name 仿真名称
     * @param description 仿真描述
     * @param inputStream 仿真数据输入流
     * @return 创建成功的Simulation对象
     * @throws Exception 当数据导入失败时抛出异常
     */
    @Transactional
    public Simulation createSimulation(String name, String description, InputStream inputStream) throws Exception {
        // 1. 创建并保存Simulation元数据
        Simulation simulation = new Simulation();
        simulation.setName(name);
        simulation.setDescription(description);
        Simulation savedSimulation = simulationRepository.save(simulation);

        // 2. 导入关联的详细数据
        try {
            simulationDataImportService.importSimulationData(inputStream, savedSimulation);
        } catch (Exception e) {
            // 如果导入失败，手动回滚事务，确保不会留下孤立的Simulation记录
            throw new Exception("Failed to import simulation data: " + e.getMessage(), e);
        }

        return savedSimulation;
    }

    /**
     * 根据ID删除仿真记录
     *
     * @param id 要删除的仿真记录ID
     * @throws RuntimeException 当指定ID的仿真记录不存在时抛出异常
     */
    @Transactional
    public void deleteSimulation(Long id) {
        if (!simulationRepository.existsById(id)) {
            throw new RuntimeException("Simulation with ID " + id + " not found.");
        }
        simulationRepository.deleteById(id);
    }
}

