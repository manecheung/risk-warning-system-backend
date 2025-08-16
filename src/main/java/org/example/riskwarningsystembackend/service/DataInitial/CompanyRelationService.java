package org.example.riskwarningsystembackend.service.DataInitial;

import lombok.extern.slf4j.Slf4j;
import org.example.riskwarningsystembackend.entity.ProductEdge;
import org.example.riskwarningsystembackend.entity.ProductNode;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.repository.company.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.company.CompanyRelationRepository;
import org.example.riskwarningsystembackend.repository.product.ProductEdgeRepository;
import org.example.riskwarningsystembackend.repository.product.ProductNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CompanyRelationService 类用于处理公司之间的关系重建逻辑。
 * 该服务通过分析公司主营产品和产品之间的上下游关系，推断出公司间的“合作”与“竞争”关系，
 * 并将这些关系存储到数据库中。
 */
@Service
@Slf4j
public class CompanyRelationService {

    private final CompanyInfoRepository companyInfoRepository;
    private final CompanyRelationRepository companyRelationRepository;
    private final ProductNodeRepository productNodeRepository;
    private final ProductEdgeRepository productEdgeRepository;

    /**
     * 构造函数，注入所需的 Repository 依赖。
     *
     * @param companyInfoRepository     公司信息数据访问接口
     * @param companyRelationRepository 公司关系数据访问接口
     * @param productNodeRepository     产品节点数据访问接口
     * @param productEdgeRepository     产品边数据访问接口
     */
    public CompanyRelationService(CompanyInfoRepository companyInfoRepository,
                                  CompanyRelationRepository companyRelationRepository,
                                  ProductNodeRepository productNodeRepository,
                                  ProductEdgeRepository productEdgeRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.companyRelationRepository = companyRelationRepository;
        this.productNodeRepository = productNodeRepository;
        this.productEdgeRepository = productEdgeRepository;
    }

    /**
     * 重建公司之间的关联关系。
     * 该方法会先清空现有的公司关系数据，然后根据公司主营产品和产品链路推断新的合作关系和竞争关系，
     * 最后将这些关系保存到数据库中。
     * <p>
     * 推断逻辑包括：
     * 1. 合作关系：如果公司A生产产品X，公司B生产产品Y，且X是Y的上游产品，则A和B存在合作关系；
     * 2. 竞争关系：如果多个公司生产相同的产品，则它们之间存在竞争关系。
     */
    @Transactional
    public void rebuildCompanyRelations() {
        log.info("开始重建公司间关联关系...");

        // 步骤 1: 清空旧的关系
        companyRelationRepository.deleteAllInBatch();
        log.info("已清空所有旧的公司关系。");

        // 步骤 2: 重新加载和推断关系
        List<CompanyInfo> allCompanies = companyInfoRepository.findAll();
        Map<String, List<CompanyInfo>> productToCompanyMap = new HashMap<>();

        // 构建产品-公司映射
        for (CompanyInfo company : allCompanies) {
            if (StringUtils.hasText(company.getMajorProduct1())) {
                productToCompanyMap.computeIfAbsent(company.getMajorProduct1(), k -> new ArrayList<>()).add(company);
            }
            if (StringUtils.hasText(company.getMajorProduct2())) {
                productToCompanyMap.computeIfAbsent(company.getMajorProduct2(), k -> new ArrayList<>()).add(company);
            }
        }

        Map<String, CompanyRelation> finalRelations = new HashMap<>();

        // 推断“合作”关系：基于产品链路（父子节点）
        List<ProductEdge> productEdges = productEdgeRepository.findAll();
        Map<Long, ProductNode> idToNodeMap = productNodeRepository.findAll().stream()
                .collect(Collectors.toMap(ProductNode::getId, Function.identity()));

        for (ProductEdge edge : productEdges) {
            ProductNode parentNode = idToNodeMap.get(edge.getParentId());
            ProductNode childNode = idToNodeMap.get(edge.getChildId());

            if (parentNode != null && childNode != null) {
                List<CompanyInfo> parentCompanies = productToCompanyMap.get(parentNode.getName());
                List<CompanyInfo> childCompanies = productToCompanyMap.get(childNode.getName());

                if (parentCompanies != null && childCompanies != null) {
                    for (CompanyInfo childCompany : childCompanies) {
                        for (CompanyInfo parentCompany : parentCompanies) {
                            if (!childCompany.getId().equals(parentCompany.getId())) {
                                CompanyRelation relation = new CompanyRelation(childCompany.getId(), parentCompany.getId(), childNode.getName(), "合作", "partner");
                                String key = relation.getCompanyOneId() + "-" + relation.getCompanyTwoId() + "-" + relation.getSharedProductName();
                                finalRelations.putIfAbsent(key, relation);
                            }
                        }
                    }
                }
            }
        }
        log.info("推断出 {} 条潜在的'合作'关系.", finalRelations.values().stream().filter(r -> r.getRelationType().equals("partner")).count());

        // 推断“竞争”关系：多个公司生产同一产品
        for (Map.Entry<String, List<CompanyInfo>> entry : productToCompanyMap.entrySet()) {
            String sharedProduct = entry.getKey();
            List<CompanyInfo> companies = entry.getValue();

            if (companies.size() > 1) {
                for (int i = 0; i < companies.size(); i++) {
                    for (int j = i + 1; j < companies.size(); j++) {
                        CompanyInfo company1 = companies.get(i);
                        CompanyInfo company2 = companies.get(j);
                        CompanyRelation relation = new CompanyRelation(company1.getId(), company2.getId(), sharedProduct, "竞争", "supplier");
                        String key = relation.getCompanyOneId() + "-" + relation.getCompanyTwoId() + "-" + relation.getSharedProductName();
                        finalRelations.put(key, relation);
                    }
                }
            }
        }

        companyRelationRepository.saveAll(finalRelations.values());
        log.info("公司关系表 (company_relations) 重建完毕，加载了 {} 条新记录.", finalRelations.size());
    }
}
