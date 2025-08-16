package org.example.riskwarningsystembackend.config;

import lombok.extern.slf4j.Slf4j;
import org.example.riskwarningsystembackend.repository.product.ProductEdgeRepository;
import org.example.riskwarningsystembackend.repository.product.ProductNodeRepository;
import org.example.riskwarningsystembackend.service.DataInitial.ProductGraphService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 产品图谱初始数据加载器
 * /p
 * 该类负责在应用程序启动时检查并初始化产品图谱数据。
 * 当检测到产品节点和边数据为空时，会触发数据加载过程。
 */
@Component
@Slf4j
@Order(2)
public class ProductGraphInitialDataLoader implements CommandLineRunner {


    private final ProductGraphService productGraphService;
    private final ProductNodeRepository productNodeRepository;
    private final ProductEdgeRepository productEdgeRepository;

    /**
     * 构造函数
     *
     * @param productGraphService   产品图谱服务，用于加载图谱数据
     * @param productNodeRepository 产品节点数据访问仓库
     * @param productEdgeRepository 产品边数据访问仓库
     */
    public ProductGraphInitialDataLoader(ProductGraphService productGraphService,
                                         ProductNodeRepository productNodeRepository,
                                         ProductEdgeRepository productEdgeRepository) {
        this.productGraphService = productGraphService;
        this.productNodeRepository = productNodeRepository;
        this.productEdgeRepository = productEdgeRepository;
    }

    /**
     * 应用程序启动时执行的数据加载方法
     * /p
     * 检查产品节点和边数据是否为空，如果为空则调用服务层方法加载初始数据
     *
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        // 检查节点和边数据是否都为空，避免重复加载数据
        if (productNodeRepository.count() == 0 && productEdgeRepository.count() == 0) {
            log.info("开始构建产品图谱数据 (nodes and edges)...");
            productGraphService.loadProductGraphData(); // 调用外部Service方法触发事务
        }
    }
}

