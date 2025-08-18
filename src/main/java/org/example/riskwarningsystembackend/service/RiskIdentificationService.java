package org.example.riskwarningsystembackend.service;

import jakarta.annotation.PostConstruct;
import org.example.riskwarningsystembackend.dto.RiskIdentificationResult;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.ProductNode;
import org.example.riskwarningsystembackend.repository.company.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.product.ProductNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 风险识别服务.
 * 负责从文本内容中识别风险关键词和关联的实体（公司、产品).
 */
@Service
public class RiskIdentificationService {

    private static final Logger logger = LoggerFactory.getLogger(RiskIdentificationService.class);
    private static final String KEYWORDS_FILE_PATH = "data/模糊匹配关键词.txt";

    private final CompanyInfoRepository companyInfoRepository;
    private final ProductNodeRepository productNodeRepository;

    private final Set<String> riskKeywords = new HashSet<>();
    private Map<String, CompanyInfo> companyMap = Collections.emptyMap();
    private Map<String, ProductNode> productMap = Collections.emptyMap();

    @Autowired
    public RiskIdentificationService(CompanyInfoRepository companyInfoRepository, ProductNodeRepository productNodeRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.productNodeRepository = productNodeRepository;
    }

    /**
     * 在服务初始化时加载所有关键词.
     */
    @PostConstruct
    public void initialize() {
        loadRiskKeywords();
        loadEntityKeywords();
    }

    /**
     * 从类路径加载风险关键词文件.
     */
    private void loadRiskKeywords() {
        ClassPathResource resource = new ClassPathResource(KEYWORDS_FILE_PATH);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !Pattern.matches("^\\d+\\..*：$", line))
                    .forEach(riskKeywords::add);
            logger.info("成功加载 {} 个风险关键词.", riskKeywords.size());
        } catch (IOException e) {
            logger.error("加载风险关键词文件失败: {}", KEYWORDS_FILE_PATH, e);
        }
    }

    /**
     * 从数据库加载公司和产品实体关键词.
     * 使用Map进行存储，便于快速查找.
     */
    public void loadEntityKeywords() {
        try {
            // 加载公司信息
            List<CompanyInfo> companies = companyInfoRepository.findAll();
            this.companyMap = companies.stream()
                    .filter(c -> c.getName() != null && !c.getName().trim().isEmpty())
                    .collect(Collectors.toMap(CompanyInfo::getName, Function.identity(), (existing, replacement) -> existing));
            logger.info("成功加载 {} 个公司实体.", this.companyMap.size());

            // 加载产品信息
            List<ProductNode> products = productNodeRepository.findAll();
            this.productMap = products.stream()
                    .filter(p -> p.getName() != null && !p.getName().trim().isEmpty())
                    .collect(Collectors.toMap(ProductNode::getName, Function.identity(), (existing, replacement) -> existing));
            logger.info("成功加载 {} 个产品实体.", this.productMap.size());
        } catch (Exception e) {
            logger.error("从数据库加载实体关键词失败.", e);
        }
    }

    /**
     * 对给定的文本内容进行风险识别.
     *
     * @param textContent 新闻文章的内容.
     * @return 风险识别结果 DTO.
     */
    public RiskIdentificationResult identifyRisk(String textContent) {
        RiskIdentificationResult result = new RiskIdentificationResult();
        result.setRisk(false);

        if (textContent == null || textContent.isEmpty()) {
            return result;
        }

        // 1. 匹配风险关键词
        Set<String> matchedKeywords = riskKeywords.stream()
                .filter(textContent::contains)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(matchedKeywords)) {
            return result;
        }

        // 2. 匹配公司实体
        Set<CompanyInfo> matchedCompanies = companyMap.entrySet().stream()
                .filter(entry -> textContent.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        // 3. 匹配产品实体
        Set<ProductNode> matchedProducts = productMap.entrySet().stream()
                .filter(entry -> textContent.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        // 4. 最终判定
        if (!CollectionUtils.isEmpty(matchedCompanies) || !CollectionUtils.isEmpty(matchedProducts)) {
            result.setRisk(true);
            result.setMatchedRiskKeywords(matchedKeywords);
            result.setMatchedCompanies(matchedCompanies);
            result.setMatchedProducts(matchedProducts);
        }

        return result;
    }
}