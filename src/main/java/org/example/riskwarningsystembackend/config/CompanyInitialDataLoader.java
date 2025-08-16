package org.example.riskwarningsystembackend.config;

import org.example.riskwarningsystembackend.service.DataInitial.CompanyDataLoadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 公司初始数据加载器配置类
 * 实现CommandLineRunner接口，在Spring Boot应用启动时自动执行数据初始化操作
 * 该类负责检查并加载公司和产品相关的初始数据
 */
@Component
@Order(1)
public class CompanyInitialDataLoader implements CommandLineRunner {

    private final CompanyDataLoadService companyDataLoadService;

    /**
     * 构造函数
     *
     * @param companyDataLoadService 公司数据加载服务实例，用于执行具体的数据加载操作
     */
    public CompanyInitialDataLoader(CompanyDataLoadService companyDataLoadService) {
        this.companyDataLoadService = companyDataLoadService;
    }

    /**
     * 应用启动时执行的初始化方法
     * 检查公司数据和产品数据是否为空，如果为空则加载相应的初始数据
     *
     * @param args 命令行参数数组
     * @throws Exception 数据加载过程中可能抛出的异常
     */
    @Override
    public void run(String... args) throws Exception {
        // 检查并加载公司数据
        if (companyDataLoadService.isCompanyDataEmpty()) {
            companyDataLoadService.loadCompanyData();
        }
        // 检查并加载产品数据
        if (companyDataLoadService.isProductDataEmpty()) {
            companyDataLoadService.loadProductData();
        }
    }
}
