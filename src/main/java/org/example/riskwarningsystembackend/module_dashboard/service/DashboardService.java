package org.example.riskwarningsystembackend.module_dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.repository.CompanyRepository;
import org.example.riskwarningsystembackend.module_supply_chain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;

    public List<Map<String, Object>> getKeyMetrics() {
        long industryCount = companyRepository.countDistinctIndustry();
        long companyCount = companyRepository.count();
        long productCount = productRepository.count();
        return List.of(
                Map.of("title", "涵盖行业数", "value", industryCount, "icon", "M3 18v-6a9 9 0 0 1 18 0v6"),
                Map.of("title", "涵盖企业数", "value", companyCount, "icon", "M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"),
                Map.of("title", "涵盖产品数", "value", productCount, "icon", "M6.34 7.34 4.93 8.75 4 12l4-4-1.07-3.07-1.59 2.41z")
        );
    }

    public List<Map<String, Object>> getRiskDistribution() {
        long highRiskCount = 0;
        long mediumRiskCount = 0;
        long lowRiskCount = 0;
        List<Company> companies = companyRepository.findAll();
        for (Company c : companies) {
            String riskLevel = getCompanyRiskLevel(c);
            switch (riskLevel) {
                case "高" -> highRiskCount++;
                case "中" -> mediumRiskCount++;
                default -> lowRiskCount++;
            }
        }
        return List.of(
                Map.of("value", highRiskCount, "name", "高风险企业"),
                Map.of("value", mediumRiskCount, "name", "中风险企业"),
                Map.of("value", lowRiskCount, "name", "低风险企业")
        );
    }

    public Map<String, Object> getIndustryHealth() {
        List<Company> companies = companyRepository.findAll();
        Map<String, List<Company>> companiesByIndustry = companies.stream()
                .filter(c -> c.getIndustry() != null)
                .collect(Collectors.groupingBy(Company::getIndustry));

        Map<String, Double> industryHealthScores = new HashMap<>();
        companiesByIndustry.forEach((industry, companyList) -> {
            double totalScore = companyList.stream().mapToDouble(this::getCompanyHealthScore).sum();
            double averageScore = totalScore / companyList.size();
            industryHealthScores.put(industry, averageScore);
        });

        // 排序并格式化为API需要的结构
        List<Map.Entry<String, Double>> sortedIndustries = industryHealthScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        List<String> categories = sortedIndustries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        List<Double> values = sortedIndustries.stream().map(Map.Entry::getValue).collect(Collectors.toList());

        return Map.of("categories", categories, "values", values);
    }

    public Map<String, Object> getSupplyChainRisk() {
        // 雷达图指标定义
        List<Map<String, Object>> indicator = List.of(
                Map.of("name", "技术风险", "max", 100),
                Map.of("name", "信用风险", "max", 100),
                Map.of("name", "法律风险", "max", 100),
                Map.of("name", "财务风险", "max", 100),
                Map.of("name", "产业链风险", "max", 100), // 模拟数据
                Map.of("name", "舆情风险", "max", 100)
        );

        // 按行业分组计算各维度平均分
        List<Company> companies = companyRepository.findAll();
        Map<String, List<Company>> companiesByIndustry = companies.stream()
                .filter(c -> c.getIndustry() != null)
                .collect(Collectors.groupingBy(Company::getIndustry));

        List<Map<String, Object>> riskData = new ArrayList<>();
        companiesByIndustry.forEach((industry, companyList) -> {
            double avgTech = companyList.stream().mapToInt(c -> getRiskScoreDimension(c.getTech())).average().orElse(0);
            double avgCredit = companyList.stream().mapToInt(c -> getRiskScoreDimension(c.getCredit())).average().orElse(0);
            double avgLaw = companyList.stream().mapToInt(c -> c.getLegalCaseCount() != null ? c.getLegalCaseCount() : 0).average().orElse(0);
            double avgFinance = companyList.stream().mapToDouble(c -> c.getRevenue() != null ? c.getRevenue() : 0).average().orElse(0);
            // 模拟产业链和舆情风险
            double chainRisk = (avgTech + avgCredit + avgLaw + avgFinance) / 4;
            double sentimentRisk = companyList.stream().mapToInt(c -> c.getPublicSentimentCount() != null ? c.getPublicSentimentCount() : 0).average().orElse(0);

            List<Double> values = List.of(avgTech, avgCredit, avgLaw, avgFinance, chainRisk, sentimentRisk);
            riskData.add(Map.of("value", values, "name", industry));
        });

        return Map.of("indicator", indicator, "data", riskData);
    }

    public Map<String, Object> getRiskAnalysis(int page, int pageSize) {
        // 首先，获取并排序所有记录
        List<Map<String, Object>> allRecords = companyRepository.findAll().stream()
                .sorted(Comparator.comparingInt(this::getCompanyRiskScore).reversed())
                .map(c -> {
                    Map<String, Object> record = new java.util.HashMap<>();
                    record.put("name", c.getName());
                    String level = getCompanyRiskLevel(c);
                    String levelClass;
                    if (level.equals("高")) { levelClass = "risk-high"; }
                    else if (level.equals("中")) { levelClass = "risk-medium"; }
                    else { levelClass = "risk-low"; }
                    record.put("level", level);
                    record.put("levelClass", levelClass);
                    record.put("reason", c.getReason());
                    return record;
                }).collect(Collectors.toList());

        // 手动进行分页
        int totalRecords = allRecords.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalRecords);

        // 防止索引越界
        List<Map<String, Object>> pageRecords = fromIndex >= totalRecords ? Collections.emptyList() : allRecords.subList(fromIndex, toIndex);

        // 构建符合API文档的响应结构
        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalRecords", totalRecords);
        response.put("totalPages", totalPages);
        response.put("hasPrevPage", page > 1);
        response.put("hasNextPage", page < totalPages);
        response.put("records", pageRecords);

        return response;
    }

    public List<Map<String, Object>> getRiskMap() {
        return companyRepository.findAll().stream()
                .filter(c -> c.getLongitude() != null && c.getLatitude() != null)
                .map(c -> Map.of(
                        "name", c.getName(),
                        "value", List.of(c.getLongitude(), c.getLatitude(), getCompanyRiskScore(c) * 10), // 用风险分值控制大小
                        "risk", getCompanyRiskLevel(c)
                )).collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private int getCompanyRiskScore(Company c) {
        int score = 0;
        if (c.getLegalCaseCount() != null) {
            score += c.getLegalCaseCount() / 100; // 100 cases = 1 risk point
        }
        if (c.getPublicSentimentCount() != null) {
            score += c.getPublicSentimentCount() / 500; // 500 sentiments = 1 risk point
        }
        return score;
    }

    private double getCompanyHealthScore(Company c) {
        // 健康度与风险分值成反比
        return 100.0 - getCompanyRiskScore(c) * 8.0; // 调整系数使分布更合理
    }

    private String getCompanyRiskLevel(Company c) {
        int score = getCompanyRiskScore(c);
        if (score >= 8) return "高";
        if (score >= 5) return "中";
        return "低";
    }

    private int getRiskScoreDimension(String riskLevel) {
        if (riskLevel == null) return 33; // 默认为低风险
        return switch (riskLevel.toLowerCase()) {
            case "高" -> 90;
            case "中" -> 60;
            case "低" -> 30;
            default -> 30;
        };
    }
}