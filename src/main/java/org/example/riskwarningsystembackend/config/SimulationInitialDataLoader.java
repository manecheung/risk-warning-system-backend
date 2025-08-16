package org.example.riskwarningsystembackend.config;

import lombok.extern.slf4j.Slf4j;
import org.example.riskwarningsystembackend.repository.SimulationRepository;
import org.example.riskwarningsystembackend.service.SimulationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * 默认初始模拟数据加载器
 * 在应用首次启动且数据库为空时，自动加载一套默认的模拟数据。
 */
@Slf4j
@Configuration
public class SimulationInitialDataLoader {

    @Bean
    public CommandLineRunner initialSimulationDataRunner(SimulationService simulationService, SimulationRepository simulationRepository) {
        return args -> {
            // 幂等性检查：如果数据库中已有任何模拟场景，则跳过
            if (simulationRepository.count() > 0) {
                log.info("数据库中已存在模拟场景，跳过默认数据导入。");
                return;
            }

            log.info("数据库为空，开始导入默认初始模拟数据...");
            String resourcePath = "data/风险蔓延模拟步进数据.txt";
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                log.warn("未找到默认模拟数据文件: {}", resourcePath);
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                simulationService.createSimulation(
                        "默认初始数据",
                        "自动加载的初始模拟数据。",
                        inputStream
                );
                log.info("默认初始模拟数据导入成功。");
            } catch (Exception e) {
                log.error("导入默认初始模拟数据时发生错误: {}", e.getMessage(), e);
            }
        };
    }
}
