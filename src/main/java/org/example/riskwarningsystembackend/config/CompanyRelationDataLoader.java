package org.example.riskwarningsystembackend.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(3)
public class CompanyRelationDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CompanyRelationDataLoader.class);

    private final CompanyInfoRepository companyInfoRepository;
    private final CompanyRelationRepository companyRelationRepository;
    private final ProductNodeRepository productNodeRepository;
    private final ProductEdgeRepository productEdgeRepository;

    public CompanyRelationDataLoader(CompanyInfoRepository companyInfoRepository,
                                     CompanyRelationRepository companyRelationRepository,
                                     ProductNodeRepository productNodeRepository,
                                     ProductEdgeRepository productEdgeRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.companyRelationRepository = companyRelationRepository;
        this.productNodeRepository = productNodeRepository;
        this.productEdgeRepository = productEdgeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (companyRelationRepository.count() == 0) {
            logger.info("开始构建公司间关联关系...");
            loadCompanyRelations();
        }
    }

    private void loadCompanyRelations() {
        List<CompanyInfo> allCompanies = companyInfoRepository.findAll();
        Map<String, List<CompanyInfo>> productToCompanyMap = new HashMap<>();

        // 步骤1 构建产品-公司映射
        for (CompanyInfo company : allCompanies) {
            if (StringUtils.hasText(company.getMajorProduct1())) {
                productToCompanyMap.computeIfAbsent(company.getMajorProduct1(), k -> new ArrayList<>()).add(company);
            }
            if (StringUtils.hasText(company.getMajorProduct2())) {
                productToCompanyMap.computeIfAbsent(company.getMajorProduct2(), k -> new ArrayList<>()).add(company);
            }
        }

        // Use a Map to enforce uniqueness and precedence (Competition > Cooperation)
        Map<String, CompanyRelation> finalRelations = new HashMap<>();

        // Step 2: Infer "Cooperation" relationships (lower precedence)
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
                                // The relation context is the child product (the item being supplied)
                                CompanyRelation relation = new CompanyRelation(childCompany.getId(), parentCompany.getId(), childNode.getName(), "合作");
                                // Use a consistent key. The constructor already sorts the IDs.
                                String key = relation.getCompanyOneId() + "-" + relation.getCompanyTwoId() + "-" + relation.getSharedProductName();
                                finalRelations.putIfAbsent(key, relation);
                            }
                        }
                    }
                }
            }
        }
        logger.info("推断出 {} 条潜在的'合作'关系.", finalRelations.size());

        // Step 3: Infer "Competition" relationships (higher precedence)
        for (Map.Entry<String, List<CompanyInfo>> entry : productToCompanyMap.entrySet()) {
            String sharedProduct = entry.getKey();
            List<CompanyInfo> companies = entry.getValue();

            if (companies.size() > 1) {
                for (int i = 0; i < companies.size(); i++) {
                    for (int j = i + 1; j < companies.size(); j++) {
                        CompanyInfo company1 = companies.get(i);
                        CompanyInfo company2 = companies.get(j);
                        CompanyRelation relation = new CompanyRelation(company1.getId(), company2.getId(), sharedProduct, "竞争");
                        String key = relation.getCompanyOneId() + "-" + relation.getCompanyTwoId() + "-" + relation.getSharedProductName();
                        // Using put() will overwrite any existing cooperation relation with a competition one.
                        finalRelations.put(key, relation);
                    }
                }
            }
        }
        
        companyRelationRepository.saveAll(finalRelations.values());
        logger.info("公司关系表 (company_relations) 创建并加载了 {} 条总记录.", finalRelations.size());
    }
}
