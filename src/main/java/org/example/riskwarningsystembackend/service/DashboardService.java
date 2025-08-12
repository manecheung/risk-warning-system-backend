package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.CompanyRelationRepository;
import org.example.riskwarningsystembackend.repository.ProductNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final CompanyInfoRepository companyInfoRepository;
    private final ProductNodeRepository productNodeRepository;
    private final CompanyRelationRepository companyRelationRepository;

    public DashboardService(CompanyInfoRepository companyInfoRepository,
                            ProductNodeRepository productNodeRepository,
                            CompanyRelationRepository companyRelationRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.productNodeRepository = productNodeRepository;
        this.companyRelationRepository = companyRelationRepository;
    }

    public List<KeyMetricDTO> getKeyMetrics() {
        long companyCount = companyInfoRepository.count();
        long productCount = productNodeRepository.count();
        long industryCount = companyInfoRepository.countDistinctIndustry();

        return Arrays.asList(
                new KeyMetricDTO("涵盖行业数", industryCount, "M13 10V3L4 14h7v7l9-11h-7z"),
                new KeyMetricDTO("涵盖企业数", companyCount, "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M15 21v-1a6 6 0 00-5.197-5.983"),
                new KeyMetricDTO("涵盖产品数", productCount, "M21 16V8a2 2 0 00-1-1.732l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.732l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z")
        );
    }

    public List<RiskDistributionDTO> getRiskDistribution() {
        long highRiskCount = companyInfoRepository.countByLegalDisputeCountGreaterThanEqual(500);
        long mediumRiskCount = companyInfoRepository.countByLegalDisputeCountBetween(101, 499);
        long lowRiskCount = companyInfoRepository.countByLegalDisputeCountLessThanEqual(100);

        return Arrays.asList(
                new RiskDistributionDTO(highRiskCount, "高风险企业"),
                new RiskDistributionDTO(mediumRiskCount, "中风险企业"),
                new RiskDistributionDTO(lowRiskCount, "低风险企业")
        );
    }

    public IndustryHealthDTO getIndustryHealth() {
        List<String> categories = companyInfoRepository.findTop10IndustriesByCompanyCount();
        List<Integer> values = new ArrayList<>();
        for (String industry : categories) {
            Double avgDisputes = companyInfoRepository.findAverageLegalDisputesByIndustry(industry);
            if (avgDisputes == null) {
                avgDisputes = 0.0;
            }
            int score = (int) Math.max(10, 100 - (avgDisputes / 10));
            values.add(score);
        }
        return new IndustryHealthDTO(categories, values);
    }

    public SupplyChainRiskDTO getSupplyChainRisk() {
        List<SupplyChainRiskDTO.Indicator> indicators = Arrays.asList(
                new SupplyChainRiskDTO.Indicator("技术风险", 100),
                new SupplyChainRiskDTO.Indicator("信用风险", 100),
                new SupplyChainRiskDTO.Indicator("法律风险", 100),
                new SupplyChainRiskDTO.Indicator("财务风险", 100),
                new SupplyChainRiskDTO.Indicator("产业链风险", 100),
                new SupplyChainRiskDTO.Indicator("舆情风险", 100)
        );

        String windIndustryName = "";
        Double avgLegal = companyInfoRepository.findAverageLegalDisputesByIndustry(windIndustryName);
        Double avgPublicOpinion = companyInfoRepository.findAveragePublicOpinionByIndustry(windIndustryName);
        long financialRiskCompanies = companyInfoRepository.countFinancialRiskCompaniesByIndustry(windIndustryName);
        long totalCompanies = companyInfoRepository.countByIndustry(windIndustryName);

        int legalRiskScore = (avgLegal == null) ? 80 : Math.max(10, 100 - (int)(avgLegal / 5));
        int publicOpinionScore = (avgPublicOpinion == null) ? 85 : Math.max(10, 100 - (int)(avgPublicOpinion / 2));
        int financialRiskScore = (totalCompanies == 0) ? 75 : Math.max(10, 100 - (int)((double)financialRiskCompanies / totalCompanies * 100));

        List<Integer> windPowerValues = Arrays.asList(85, 90, legalRiskScore, financialRiskScore, 95, publicOpinionScore);
        List<SupplyChainRiskDTO.ChainData> data = List.of(new SupplyChainRiskDTO.ChainData(windPowerValues, "风电行业产业链"));

        return new SupplyChainRiskDTO(indicators, data);
    }

    public PaginatedResponseDto<RiskAnalysisDto> getRiskAnalysis(PageRequest pageRequest) {
        Page<CompanyInfo> highRiskCompanies = companyInfoRepository.findHighRiskCompanies(pageRequest);
        Page<RiskAnalysisDto> riskAnalysisDtoPage = highRiskCompanies.map(company -> {
            String riskLevel;
            String reason = "";
            int legalDisputes = company.getLegalDisputeCount() != null ? company.getLegalDisputeCount() : 0;

            if (legalDisputes > 500) {
                riskLevel = "高";
                reason = "存在 " + legalDisputes + " 起法律诉讼";
            } else if (legalDisputes > 100) {
                riskLevel = "中";
                reason = "存在 " + legalDisputes + " 起法律诉讼";
            } else {
                riskLevel = "低";
            }

            if ("0".equals(company.getRevenue()) || "0".equals(company.getProfit()) || !StringUtils.hasText(company.getRevenue()) || !StringUtils.hasText(company.getProfit())) {
                riskLevel = "高";
                String financialReason = "关键财务数据缺失";
                reason = reason.isEmpty() ? financialReason : reason + "；" + financialReason;
            }

            String levelClass = "risk-" + (riskLevel.equals("高") ? "high" : (riskLevel.equals("中") ? "medium" : "low"));
            return new RiskAnalysisDto(company.getName(), riskLevel, levelClass, reason);
        });
        return new PaginatedResponseDto<>(riskAnalysisDtoPage);
    }

    public List<RiskMapDTO> getRiskMap() {
        List<CompanyInfo> companies = companyInfoRepository.findAllWithCoordinates();
        return companies.stream().map(company -> {
            String riskLevel;
            int legalDisputes = company.getLegalDisputeCount() != null ? company.getLegalDisputeCount() : 0;
            if (legalDisputes > 500) {
                riskLevel = "高";
            } else if (legalDisputes > 100) {
                riskLevel = "中";
            } else {
                riskLevel = "低";
            }
            List<Object> value = Arrays.asList(company.getLongitude(), company.getLatitude(), 20);
            return new RiskMapDTO(company.getName(), value, riskLevel);
        }).collect(Collectors.toList());
    }

    /**
     * 获取公司知识图谱数据的主入口方法。
     * 根据传入的参数决定加载方式：
     * 1. 如果提供了 companyId，则加载该公司的子图（按需展开）。
     * 2. 如果提供了 keyword，则加载与搜索结果相关的子图。
     * 3. 如果都没有提供，则加载一个有限的初始图谱。
     * @param companyId 可选参数，用于按需加载的公司ID。
     * @param keyword 可选参数，用于搜索的公司名称关键词。
     * @return 公司知识图谱DTO
     */
    public CompanyGraphDTO getCompanyKnowledgeGraph(Long companyId, String keyword) {
        if (companyId != null) {
            // 场景一：用户点击节点，按需展开
            return getSubgraphForCompany(companyId);
        } else if (StringUtils.hasText(keyword)) {
            // 场景二：用户使用搜索功能
            return getGraphBySearch(keyword);
        } else {
            // 场景三：页面初始加载
            return getInitialGraph();
        }
    }

    /**
     * 构建并返回一个规模有限的初始知识图谱。
     * 这个方法用于首次加载页面时，避免一次性返回所有数据。
     */
    private CompanyGraphDTO getInitialGraph() {
        // 限制初始加载的公司数量为50家，可以根据前端性能调整这个数值
        PageRequest pageRequest = PageRequest.of(0, 50);
        Page<CompanyInfo> companyPage = companyInfoRepository.findAll(pageRequest);
        List<CompanyInfo> companies = companyPage.getContent();
        if (companies.isEmpty()) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }

        Set<Long> companyIds = companies.stream().map(CompanyInfo::getId).collect(Collectors.toSet());

        // 高效地仅查询这部分公司之间的关系
        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(companyIds);

        return buildGraphDTO(companies, relations);
    }

    /**
     * 根据指定的公司ID，构建并返回其所有直接关联的子图。
     * 这个方法用于实现点击节点按需展开的功能。
     */
    private CompanyGraphDTO getSubgraphForCompany(Long companyId) {
        // 验证中心节点是否存在
        if (!companyInfoRepository.existsById(companyId)) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }

        // 使用Repository方法，高效地查找所有与该公司相关的关系
        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyId(companyId);

        // 从关系中提取所有关联公司的ID
        Set<Long> relatedCompanyIds = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        // 确保中心公司本身也被包含在内
        relatedCompanyIds.add(companyId);

        // 一次性从数据库批量查询所有相关公司的信息，以提高效率
        List<CompanyInfo> companies = companyInfoRepository.findAllById(relatedCompanyIds);

        return buildGraphDTO(companies, relations);
    }

    /**
     * 根据搜索关键词，构建并返回相关的子图（搜索结果+一度关系邻居）。
     */
    private CompanyGraphDTO getGraphBySearch(String keyword) {
        // 1. 根据关键词模糊查询匹配的公司
        List<CompanyInfo> matchedCompanies = companyInfoRepository.findByNameContainingIgnoreCase(keyword);
        if (matchedCompanies.isEmpty()) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }
        Set<Long> matchedCompanyIds = matchedCompanies.stream().map(CompanyInfo::getId).collect(Collectors.toSet());

        // 2. 高效地找到这些匹配公司的所有一度关联关系
        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(matchedCompanyIds);

        // 3. 从关系中提取所有涉及到的公司ID（包括搜索结果本身和它们的邻居）
        Set<Long> allCompanyIdsInGraph = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        allCompanyIdsInGraph.addAll(matchedCompanyIds); // 确保搜索结果公司也被包含

        // 4. 批量查询所有需要展示的公司信息
        List<CompanyInfo> allCompanies = companyInfoRepository.findAllById(allCompanyIdsInGraph);

        return buildGraphDTO(allCompanies, relations);
    }

    /**
     * 根据公司列表和关系列表构建最终的CompanyGraphDTO对象。
     * 这是一个辅助方法，用于将实体对象转换为前端需要的DTO格式。
     */
    private CompanyGraphDTO buildGraphDTO(List<CompanyInfo> companies, List<CompanyRelation> relations) {
        List<CompanyGraphDTO.Node> nodes = companies.stream()
                .map(company -> new CompanyGraphDTO.Node(
                        String.valueOf(company.getId()),
                        company.getName(),
                        20 // 节点的默认大小
                ))
                .collect(Collectors.toList());

        List<CompanyGraphDTO.Edge> edges = relations.stream()
                .map(relation -> new CompanyGraphDTO.Edge(
                        String.valueOf(relation.getCompanyOneId()),
                        String.valueOf(relation.getCompanyTwoId()),
                        relation.getRelationName() + "(" + relation.getSharedProductName() + ")",
                        String.valueOf(relation.getRelationType())
                ))
                .collect(Collectors.toList());

        return new CompanyGraphDTO(nodes, edges);
    }
}