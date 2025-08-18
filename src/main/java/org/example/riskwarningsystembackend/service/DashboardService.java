package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.dto.dashboard.RiskMapDTO;
import org.example.riskwarningsystembackend.dto.dashboard.*;
import org.example.riskwarningsystembackend.dto.supplychain.SupplyChainRiskDTO;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.repository.company.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.company.CompanyRelationRepository;
import org.example.riskwarningsystembackend.repository.product.ProductNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 风险预警系统仪表盘服务类，提供各类风险数据统计与分析功能。
 */
@Service
public class DashboardService {

    private final CompanyInfoRepository companyInfoRepository;
    private final ProductNodeRepository productNodeRepository;
    private final CompanyRelationRepository companyRelationRepository;

    /**
     * 构造函数，注入所需的 Repository 依赖。
     *
     * @param companyInfoRepository 公司信息数据访问接口
     * @param productNodeRepository 产品节点数据访问接口
     * @param companyRelationRepository 公司关系数据访问接口
     */
    public DashboardService(CompanyInfoRepository companyInfoRepository,
                            ProductNodeRepository productNodeRepository,
                            CompanyRelationRepository companyRelationRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.productNodeRepository = productNodeRepository;
        this.companyRelationRepository = companyRelationRepository;
    }

    /**
     * 获取关键指标数据，包括涵盖行业数、企业数和产品数。
     *
     * @return 关键指标列表，每个元素包含指标名称、数值和图标路径
     */
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

    /**
     * 获取企业法律纠纷数量的风险分布情况。
     *
     * @return 风险分布列表，包含高、中、低风险企业的数量及标签
     */
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

    /**
     * 获取各行业的健康评分，基于平均法律纠纷数量计算。
     *
     * @return 行业健康评分数据对象，包含行业名称列表和对应的评分列表
     */
    public IndustryHealthDTO getIndustryHealth() {
        List<String> categories = companyInfoRepository.findTop15IndustriesByCompanyCount();
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

    /**
     * 获取供应链风险评估数据，包括多个维度的评分。
     *
     * @return 供应链风险数据传输对象，包含指标列表和具体行业链数据
     */
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

    /**
     * 分页获取高风险企业分析数据。
     *
     * @param pageRequest 分页请求参数
     * @return 包含风险分析数据的分页响应对象
     */
    public PaginatedResponseDTO<RiskAnalysisDTO> getRiskAnalysis(PageRequest pageRequest) {
        Page<CompanyInfo> highRiskCompanies = companyInfoRepository.findHighRiskCompanies(pageRequest);
        Page<RiskAnalysisDTO> riskAnalysisDtoPage = highRiskCompanies.map(company -> {
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
            return new RiskAnalysisDTO(company.getName(), riskLevel, levelClass, reason);
        });
        return new PaginatedResponseDTO<>(riskAnalysisDtoPage);
    }

    /**
     * 获取企业地理分布风险地图数据。
     *
     * @return 风险地图数据列表，每个元素包含企业名称、坐标和风险等级
     */
    public List<RiskMapDTO> getRiskMap() {
        List<CompanyInfo> companies = companyInfoRepository.findAllWithCoordinates();
        return companies.stream()
                .filter(company -> company.getLongitude() != null && company.getLatitude() != null && company.getLongitude() != 0 && company.getLatitude() != 0) // 过滤无效坐标
                .map(company -> {
                    String riskLevel;
                    int legalDisputes = company.getLegalDisputeCount() != null ? company.getLegalDisputeCount() : 0;
                    if (legalDisputes > 500) {
                        riskLevel = "高";
                    } else if (legalDisputes > 100) {
                        riskLevel = "中";
                    } else {
                        riskLevel = "低";
                    }
                    // 修正经纬度顺序：[经度, 纬度, 数值]
                    List<Object> value = Arrays.asList(company.getLongitude(), company.getLatitude(), 40);
                    return new RiskMapDTO(company.getName(), value, riskLevel);
                }).collect(Collectors.toList());
    }

    /**
     * 根据公司ID或关键词获取企业知识图谱数据。
     *
     * @param companyId 指定的公司ID（可为空）
     * @param keyword 搜索关键词（可为空）
     * @return 企业知识图谱数据对象，包含节点和边信息
     */
    public CompanyGraphDTO getCompanyKnowledgeGraph(Long companyId, String keyword) {
        if (companyId != null) {
            return getSubgraphForCompany(companyId);
        } else if (StringUtils.hasText(keyword)) {
            return getGraphBySearch(keyword);
        } else {
            return getInitialGraph();
        }
    }

    /**
     * 构建初始知识图谱数据。
     *
     * @return 初始图谱数据对象
     */
    private CompanyGraphDTO getInitialGraph() {
        PageRequest pageRequest = PageRequest.of(0, 60);
        Page<CompanyInfo> companyPage = companyInfoRepository.findAll(pageRequest);
        List<CompanyInfo> initialCompanies = companyPage.getContent();
        if (initialCompanies.isEmpty()) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }

        Set<Long> initialCompanyIds = initialCompanies.stream().map(CompanyInfo::getId).collect(Collectors.toSet());
        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(initialCompanyIds);
        if (relations.size() > 60) {
            relations = relations.subList(0, 60);
        }

        Set<Long> allCompanyIdsInGraph = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        allCompanyIdsInGraph.addAll(initialCompanyIds);

        List<CompanyInfo> allCompanies = companyInfoRepository.findAllById(allCompanyIdsInGraph);

        return buildGraphDTO(allCompanies, relations);
    }

    /**
     * 根据指定公司ID构建子图谱数据。
     *
     * @param companyId 指定公司的ID
     * @return 子图谱数据对象
     */
    private CompanyGraphDTO getSubgraphForCompany(Long companyId) {
        if (!companyInfoRepository.existsById(companyId)) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }

        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyId(companyId);
        Set<Long> relatedCompanyIds = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        relatedCompanyIds.add(companyId);

        List<CompanyInfo> companies = companyInfoRepository.findAllById(relatedCompanyIds);

        return buildGraphDTO(companies, relations);
    }

    /**
     * 根据关键词搜索匹配的企业并构建图谱数据。
     *
     * @param keyword 搜索关键词
     * @return 匹配结果的图谱数据对象
     */
    private CompanyGraphDTO getGraphBySearch(String keyword) {
        Page<CompanyInfo> matchedCompaniesPage = companyInfoRepository.findByNameContainingIgnoreCase(keyword, PageRequest.of(0, 50));
        List<CompanyInfo> matchedCompanies = matchedCompaniesPage.getContent();
        if (matchedCompanies.isEmpty()) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }
        Set<Long> matchedCompanyIds = matchedCompanies.stream().map(CompanyInfo::getId).collect(Collectors.toSet());

        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(matchedCompanyIds);
        if (relations.size() > 60) {
            relations = relations.subList(0, 60);
        }

        Set<Long> allCompanyIdsInGraph = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        allCompanyIdsInGraph.addAll(matchedCompanyIds);

        List<CompanyInfo> allCompanies = companyInfoRepository.findAllById(allCompanyIdsInGraph);

        return buildGraphDTO(allCompanies, relations);
    }

    /**
     * 构建图谱数据传输对象。
     *
     * @param companies 公司信息列表
     * @param relations 公司关系列表
     * @return 图谱数据对象，包含节点和边信息
     */
    private CompanyGraphDTO buildGraphDTO(List<CompanyInfo> companies, List<CompanyRelation> relations) {
        List<CompanyGraphDTO.Node> nodes = companies.stream()
                .map(company -> new CompanyGraphDTO.Node(
                        String.valueOf(company.getId()),
                        company.getName(),
                        20
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

