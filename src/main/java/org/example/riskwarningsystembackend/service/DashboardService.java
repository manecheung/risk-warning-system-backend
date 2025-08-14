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

    public CompanyGraphDTO getCompanyKnowledgeGraph(Long companyId, String keyword) {
        if (companyId != null) {
            return getSubgraphForCompany(companyId);
        } else if (StringUtils.hasText(keyword)) {
            return getGraphBySearch(keyword);
        } else {
            return getInitialGraph();
        }
    }

    private CompanyGraphDTO getInitialGraph() {
        PageRequest pageRequest = PageRequest.of(0, 50);
        Page<CompanyInfo> companyPage = companyInfoRepository.findAll(pageRequest);
        List<CompanyInfo> initialCompanies = companyPage.getContent();
        if (initialCompanies.isEmpty()) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }

        Set<Long> initialCompanyIds = initialCompanies.stream().map(CompanyInfo::getId).collect(Collectors.toSet());
        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(initialCompanyIds);

        Set<Long> allCompanyIdsInGraph = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        allCompanyIdsInGraph.addAll(initialCompanyIds);

        List<CompanyInfo> allCompanies = companyInfoRepository.findAllById(allCompanyIdsInGraph);

        return buildGraphDTO(allCompanies, relations);
    }

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

    private CompanyGraphDTO getGraphBySearch(String keyword) {
        List<CompanyInfo> matchedCompanies = companyInfoRepository.findByNameContainingIgnoreCase(keyword);
        if (matchedCompanies.isEmpty()) {
            return new CompanyGraphDTO(Collections.emptyList(), Collections.emptyList());
        }
        Set<Long> matchedCompanyIds = matchedCompanies.stream().map(CompanyInfo::getId).collect(Collectors.toSet());

        List<CompanyRelation> relations = companyRelationRepository.findAllByCompanyIds(matchedCompanyIds);

        Set<Long> allCompanyIdsInGraph = relations.stream()
                .flatMap(relation -> Stream.of(relation.getCompanyOneId(), relation.getCompanyTwoId()))
                .collect(Collectors.toSet());
        allCompanyIdsInGraph.addAll(matchedCompanyIds);

        List<CompanyInfo> allCompanies = companyInfoRepository.findAllById(allCompanyIdsInGraph);

        return buildGraphDTO(allCompanies, relations);
    }

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
