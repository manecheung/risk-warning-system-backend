package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.entity.ProductEdge;
import org.example.riskwarningsystembackend.entity.ProductNode;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.CompanyRelationRepository;
import org.example.riskwarningsystembackend.repository.ProductEdgeRepository;
import org.example.riskwarningsystembackend.repository.ProductNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompanyRelationService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyRelationService.class);

    private final CompanyInfoRepository companyInfoRepository;
    private final CompanyRelationRepository companyRelationRepository;
    private final ProductNodeRepository productNodeRepository;
    private final ProductEdgeRepository productEdgeRepository;

    public CompanyRelationService(CompanyInfoRepository companyInfoRepository,
                                  CompanyRelationRepository companyRelationRepository,
                                  ProductNodeRepository productNodeRepository,
                                  ProductEdgeRepository productEdgeRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.companyRelationRepository = companyRelationRepository;
        this.productNodeRepository = productNodeRepository;
        this.productEdgeRepository = productEdgeRepository;
    }

    @Transactional
    public void rebuildCompanyRelations() {
        logger.info("开始重建公司间关联关系...");

        // 步骤 1: 清空旧的关系
        companyRelationRepository.deleteAllInBatch();
        logger.info("已清空所有旧的公司关系。");

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

        // 推断“合作”关系
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
        logger.info("推断出 {} 条潜在的'合作'关系.", finalRelations.values().stream().filter(r -> r.getRelationType().equals("partner")).count());

        // 推断“竞争”关系
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
        logger.info("公司关系表 (company_relations) 重建完毕，加载了 {} 条新记录.", finalRelations.size());
    }
}
