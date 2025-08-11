package org.example.riskwarningsystembackend.module_supply_chain.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 供应链服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupplyChainService {

    private final CompanyRepository companyRepository;

    /**
     * 获取企业列表（分页+搜索）
     * @param pageable 分页参数
     * @param keyword 搜索关键词
     * @return 企业分页数据
     */
    @Transactional(readOnly = true)
    public Page<Company> getCompanies(Pageable pageable, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return companyRepository.findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(keyword, keyword, pageable);
        }
        return companyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, Company companyDetails) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new RuntimeException("未找到ID为 " + id + " 的企业"));
        company.setName(companyDetails.getName());
        company.setIndustry(companyDetails.getIndustry());
        company.setTech(companyDetails.getTech());
        company.setFinance(companyDetails.getFinance());
        company.setLaw(companyDetails.getLaw());
        company.setCredit(companyDetails.getCredit());
        company.setReason(companyDetails.getReason());
        company.setRegisteredCapital(companyDetails.getRegisteredCapital());
        company.setPaidInCapital(companyDetails.getPaidInCapital());
        company.setScale(companyDetails.getScale());
        company.setEmployeeCount(companyDetails.getEmployeeCount());
        company.setCertificateCount(companyDetails.getCertificateCount());
        company.setTaxRating(companyDetails.getTaxRating());
        company.setPublicSentimentCount(companyDetails.getPublicSentimentCount());
        company.setLegalCaseCount(companyDetails.getLegalCaseCount());
        company.setStockIndex(companyDetails.getStockIndex());
        company.setRevenue(companyDetails.getRevenue());
        company.setAssets(companyDetails.getAssets());
        company.setProfit(companyDetails.getProfit());
        company.setRegisteredAddress(companyDetails.getRegisteredAddress());
        company.setLongitude(companyDetails.getLongitude());
        company.setLatitude(companyDetails.getLatitude());
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    /**
     * 6.1 获取供应链风险概要
     * @return 包含风险统计数据的Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary() {
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

        // 综合风险评估逻辑（简化）
        String networkRisk = "低";
        if (highRiskCount > companies.size() * 0.1 || mediumRiskCount > companies.size() * 0.3) {
            networkRisk = "高";
        } else if (highRiskCount > 0 || mediumRiskCount > companies.size() * 0.1) {
            networkRisk = "中";
        }

        return Map.of(
                "networkRisk", networkRisk,
                "highRiskCount", highRiskCount,
                "mediumRiskCount", mediumRiskCount,
                "lowRiskCount", lowRiskCount
        );
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

    private String getCompanyRiskLevel(Company c) {
        int score = getCompanyRiskScore(c);
        if (score >= 8) return "高";
        if (score >= 5) return "中";
        return "低";
    }
}
