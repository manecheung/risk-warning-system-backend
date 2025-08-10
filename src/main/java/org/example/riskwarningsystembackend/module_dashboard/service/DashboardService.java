package org.example.riskwarningsystembackend.module_dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.repository.CompanyRepository;
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

    public List<Map<String, Object>> getKeyMetrics() {
        long industryCount = companyRepository.countDistinctIndustry();
        long companyCount = companyRepository.count();
        long productCount = 12833L; // Mock - 产品数据不在当前模型中
        return List.of(
                Map.of("title", "涵盖行业数", "value", industryCount),
                Map.of("title", "涵盖企业数", "value", companyCount),
                Map.of("title", "涵盖产品数", "value", productCount)
        );
    }

    public List<Map<String, Object>> getRiskDistribution() {
        long highRiskCount = 0;
        long mediumRiskCount = 0;
        long lowRiskCount = 0;
        List<Company> companies = companyRepository.findAll();
        for (Company c : companies) {
            int riskScore = getCompanyRiskScore(c);
            if (riskScore >= 8) { // 定义高风险阈值
                highRiskCount++;
            } else if (riskScore >= 5) { // 定义中风险阈值
                mediumRiskCount++;
            } else {
                lowRiskCount++;
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
                Map.of("name", "舆情风险", "max", 100)  // 模拟数据
        );

        // 按行业分组计算各维度平均分
        List<Company> companies = companyRepository.findAll();
        Map<String, List<Company>> companiesByIndustry = companies.stream()
                .filter(c -> c.getIndustry() != null)
                .collect(Collectors.groupingBy(Company::getIndustry));

        List<Map<String, Object>> riskData = new ArrayList<>();
        companiesByIndustry.forEach((industry, companyList) -> {
            double avgTech = companyList.stream().mapToInt(c -> convertRiskToScore(c.getTech())).average().orElse(0);
            double avgCredit = companyList.stream().mapToInt(c -> convertRiskToScore(c.getCredit())).average().orElse(0);
            double avgLaw = companyList.stream().mapToInt(c -> convertRiskToScore(c.getLaw())).average().orElse(0);
            double avgFinance = companyList.stream().mapToInt(c -> convertRiskToScore(c.getFinance())).average().orElse(0);
            // 模拟产业链和舆情风险
            double chainRisk = (avgTech + avgCredit + avgLaw + avgFinance) / 4;
            double sentimentRisk = new Random().nextInt(40) + 40; // 40-80的随机数

            List<Double> values = List.of(avgTech, avgCredit, avgLaw, avgFinance, chainRisk, sentimentRisk);
            riskData.add(Map.of("value", values, "name", industry));
        });

        return Map.of("indicator", indicator, "data", riskData);
    }

    public List<Map<String, Object>> getRiskAnalysis() {
        return companyRepository.findAll().stream()
                .sorted(Comparator.comparingInt(this::getCompanyRiskScore).reversed())
                .limit(10)
                .map(c -> {
                    Map<String, Object> record = new java.util.HashMap<>();
                    record.put("name", c.getName());
                    int score = getCompanyRiskScore(c);
                    String level;
                    String levelClass;
                    if (score >= 8) { level = "高"; levelClass = "risk-high"; }
                    else if (score >= 5) { level = "中"; levelClass = "risk-medium"; }
                    else { level = "低"; levelClass = "risk-low"; }
                    record.put("level", level);
                    record.put("levelClass", levelClass);
                    record.put("reason", c.getReason());
                    return record;
                }).collect(Collectors.toList());
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
        return convertRiskToScore(c.getTech())/10 +
               convertRiskToScore(c.getFinance())/10 +
               convertRiskToScore(c.getLaw())/10 +
               convertRiskToScore(c.getCredit())/10;
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

    private int convertRiskToScore(String riskLevel) {
        if (riskLevel == null) return 33; // 默认为低风险
        return switch (riskLevel.toLowerCase()) {
            case "高" -> 90;
            case "中" -> 60;
            case "低" -> 30;
            default -> 30;
        };
    }
}