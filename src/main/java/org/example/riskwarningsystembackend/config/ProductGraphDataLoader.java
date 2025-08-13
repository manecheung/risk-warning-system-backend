package org.example.riskwarningsystembackend.config;

import org.example.riskwarningsystembackend.entity.ProductEdge;
import org.example.riskwarningsystembackend.entity.ProductInfo;
import org.example.riskwarningsystembackend.entity.ProductNode;
import org.example.riskwarningsystembackend.repository.ProductEdgeRepository;
import org.example.riskwarningsystembackend.repository.ProductInfoRepository;
import org.example.riskwarningsystembackend.repository.ProductNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(2)
public class ProductGraphDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductGraphDataLoader.class);

    private final ProductInfoRepository productInfoRepository;
    private final ProductNodeRepository productNodeRepository;
    private final ProductEdgeRepository productEdgeRepository;

    public ProductGraphDataLoader(ProductInfoRepository productInfoRepository,
                                ProductNodeRepository productNodeRepository,
                                ProductEdgeRepository productEdgeRepository) {
        this.productInfoRepository = productInfoRepository;
        this.productNodeRepository = productNodeRepository;
        this.productEdgeRepository = productEdgeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productNodeRepository.count() == 0 && productEdgeRepository.count() == 0) {
            logger.info("开始构建产品图谱数据 (nodes and edges)...");
            loadProductGraphData();
        }
    }

    private void loadProductGraphData() {
        List<ProductInfo> allProductInfos = productInfoRepository.findAll();

        // 第 1 步：收集所有唯一的产品名称及其最高层级（最小的层级编号）。
        Map<String, Integer> productLevels = new HashMap<>();
        for (ProductInfo productInfo : allProductInfos) {
            updateNodeLevel(productLevels, productInfo.getLevel1(), 1);
            updateNodeLevel(productLevels, productInfo.getLevel2(), 2);
            updateNodeLevel(productLevels, productInfo.getLevel3(), 3);
            updateNodeLevel(productLevels, productInfo.getLevel4(), 4);
            updateNodeLevel(productLevels, productInfo.getLevel5(), 5);
        }

        List<ProductNode> nodesToCreate = productLevels.entrySet().stream()
                .map(entry -> new ProductNode(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        productNodeRepository.saveAll(nodesToCreate);
        logger.info("产品节点表 (product_nodes) 创建并加载了 {} 条记录.", nodesToCreate.size());

        // 第 2 步：创建一个映射，以便根据产品名称快速查找对应的 ID。
        Map<String, Long> nameToIdMap = productNodeRepository.findAll().stream()
                .collect(Collectors.toMap(ProductNode::getName, ProductNode::getId));

        // 第 3 步：创建所有边（即父子关系）。
        Set<ProductEdge> edgesToCreate = new HashSet<>();
        for (ProductInfo productInfo : allProductInfos) {
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel1(), productInfo.getLevel2());
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel2(), productInfo.getLevel3());
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel3(), productInfo.getLevel4());
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel4(), productInfo.getLevel5());
        }
        productEdgeRepository.saveAll(edgesToCreate);
        logger.info("产品关系表 (product_edges) 创建并加载了 {} 条记录.", edgesToCreate.size());
    }

    private void updateNodeLevel(Map<String, Integer> productLevels, String name, int level) {
        if (StringUtils.hasText(name)) {
            productLevels.merge(name, level, Integer::min);
        }
    }

    private void addEdge(Set<ProductEdge> edges, Map<String, Long> nameToIdMap, String parentName, String childName) {
        if (StringUtils.hasText(parentName) && StringUtils.hasText(childName)) {
            Long parentId = nameToIdMap.get(parentName);
            Long childId = nameToIdMap.get(childName);
            if (parentId != null && childId != null) {
                edges.add(new ProductEdge(parentId, childId));
            }
        }
    }
}
