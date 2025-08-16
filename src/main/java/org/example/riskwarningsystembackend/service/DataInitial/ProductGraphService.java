package org.example.riskwarningsystembackend.service.DataInitial;

import org.example.riskwarningsystembackend.entity.ProductEdge;
import org.example.riskwarningsystembackend.entity.ProductInfo;
import org.example.riskwarningsystembackend.entity.ProductNode;
import org.example.riskwarningsystembackend.repository.product.ProductEdgeRepository;
import org.example.riskwarningsystembackend.repository.product.ProductInfoRepository;
import org.example.riskwarningsystembackend.repository.product.ProductNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品图谱数据初始化服务类。
 * <p>
 * 该服务用于从产品信息表中提取层级结构，构建产品节点和产品关系边，并保存到对应的数据库表中。
 * 主要包括以下功能：
 * 1. 读取所有产品信息；
 * 2. 构建产品节点（去重并记录最小层级）；
 * 3. 构建产品之间的层级关系边；
 * 4. 将节点和边批量保存至数据库。
 * </p>
 */
@Service
public class ProductGraphService {

    private static final Logger logger = LoggerFactory.getLogger(ProductGraphService.class);

    private final ProductInfoRepository productInfoRepository;
    private final ProductNodeRepository productNodeRepository;
    private final ProductEdgeRepository productEdgeRepository;

    /**
     * 构造函数，注入所需的数据访问仓库。
     *
     * @param productInfoRepository 产品信息数据访问接口
     * @param productNodeRepository 产品节点数据访问接口
     * @param productEdgeRepository 产品关系边数据访问接口
     */
    public ProductGraphService(ProductInfoRepository productInfoRepository,
                               ProductNodeRepository productNodeRepository,
                               ProductEdgeRepository productEdgeRepository) {
        this.productInfoRepository = productInfoRepository;
        this.productNodeRepository = productNodeRepository;
        this.productEdgeRepository = productEdgeRepository;
    }

    /**
     * 加载并初始化产品图谱数据。
     * <p>
     * 执行流程如下：
     * 1. 查询所有产品信息；
     * 2. 提取各层级字段，构建产品节点及其层级；
     * 3. 批量保存产品节点；
     * 4. 根据父子层级关系构建产品边；
     * 5. 批量保存产品边。
     * </p>
     * 使用事务确保数据一致性。
     */
    @Transactional
    public void loadProductGraphData() {
        long startTime = System.currentTimeMillis();
        // 查询所有产品信息
        List<ProductInfo> allProductInfos = productInfoRepository.findAll();

        // 记录每个节点名称对应的最小层级
        Map<String, Integer> productLevels = new HashMap<>();
        for (ProductInfo productInfo : allProductInfos) {
            updateNodeLevel(productLevels, productInfo.getLevel1(), 1);
            updateNodeLevel(productLevels, productInfo.getLevel2(), 2);
            updateNodeLevel(productLevels, productInfo.getLevel3(), 3);
            updateNodeLevel(productLevels, productInfo.getLevel4(), 4);
            updateNodeLevel(productLevels, productInfo.getLevel5(), 5);
        }

        // 构造并保存产品节点
        List<ProductNode> nodesToCreate = productLevels.entrySet().stream()
                .map(entry -> new ProductNode(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<ProductNode> savedNodes = productNodeRepository.saveAll(nodesToCreate);
        logger.info("产品节点表 (product_nodes) 创建并加载了 {} 条记录.", savedNodes.size());

        // 构建名称到ID的映射，用于后续构建边
        Map<String, Long> nameToIdMap = savedNodes.stream()
                .collect(Collectors.toMap(ProductNode::getName, ProductNode::getId));

        // 构造并保存产品边
        Set<ProductEdge> edgesToCreate = new LinkedHashSet<>();
        for (ProductInfo productInfo : allProductInfos) {
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel1(), productInfo.getLevel2());
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel2(), productInfo.getLevel3());
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel3(), productInfo.getLevel4());
            addEdge(edgesToCreate, nameToIdMap, productInfo.getLevel4(), productInfo.getLevel5());
        }

        productEdgeRepository.saveAll(edgesToCreate);
        logger.info("产品关系表 (product_edges) 创建并加载了 {} 条记录.", edgesToCreate.size());
        logger.info("产品图谱数据加载完成，总耗时: {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * 更新节点层级信息，保留最小层级。
     *
     * @param productLevels 节点名称到层级的映射
     * @param name          节点名称
     * @param level         当前层级
     */
    private void updateNodeLevel(Map<String, Integer> productLevels, String name, int level) {
        if (StringUtils.hasText(name)) {
            productLevels.merge(name, level, Integer::min);
        }
    }

    /**
     * 添加父子节点之间的边。
     *
     * @param edges       边集合，用于去重
     * @param nameToIdMap 名称到节点ID的映射
     * @param parentName  父节点名称
     * @param childName   子节点名称
     */
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
