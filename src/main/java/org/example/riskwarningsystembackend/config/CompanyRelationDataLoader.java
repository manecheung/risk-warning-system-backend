package org.example.riskwarningsystembackend.config;

import lombok.extern.slf4j.Slf4j;
import org.example.riskwarningsystembackend.service.DataInitial.CompanyRelationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 公司关系数据加载器配置类
 * <p>
 * 该类在Spring Boot应用启动时自动执行，负责初始化公司关系数据。
 * 通过实现CommandLineRunner接口，在应用启动完成后触发公司关系的重建操作。
 * 使用@Order(3)注解确保该加载器在启动顺序中的第三位执行。
 */
@Component
@Slf4j
@Order(3)
public class CompanyRelationDataLoader implements CommandLineRunner {

    private final CompanyRelationService companyRelationService;

    /**
     * 构造函数
     *
     * @param companyRelationService 公司关系服务实例，用于执行公司关系重建操作
     */
    public CompanyRelationDataLoader(CompanyRelationService companyRelationService) {
        this.companyRelationService = companyRelationService;
    }

    /**
     * 应用启动时执行的初始化方法
     * <p>
     * 在Spring Boot应用启动完成后，自动触发公司关系数据的重建操作。
     * 如果重建过程中发生异常，将记录错误日志但不会中断应用启动流程。
     *
     * @param args 启动参数数组
     */
    @Override
    public void run(String... args) {
        loadInitialCompanyRelations();
    }

    private void loadInitialCompanyRelations() {
        try {
            log.info("应用启动：正在触发初始公司关系构建...");
            long startTime = System.currentTimeMillis();
            companyRelationService.rebuildCompanyRelations();
            long endTime = System.currentTimeMillis();
            log.info("初始公司关系构建成功完成，耗时 {} 毫秒。", endTime - startTime);
        } catch (Exception e) {
            log.error("启动时初始公司关系构建失败。", e);
        }
    }

}
