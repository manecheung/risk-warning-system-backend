package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.CompanyListDTO;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.SupplyChainSummaryDTO;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SupplyChainService {

    private final CompanyInfoRepository companyInfoRepository;

    public SupplyChainService(CompanyInfoRepository companyInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
    }

    public SupplyChainSummaryDTO getSummary() {
        long highRiskCount = companyInfoRepository.countByLegalDisputeCountGreaterThanEqual(500);
        long mediumRiskCount = companyInfoRepository.countByLegalDisputeCountBetween(101, 499);
        long lowRiskCount = companyInfoRepository.countByLegalDisputeCountLessThanEqual(100);
        String networkRisk = highRiskCount > 50 ? "高" : (mediumRiskCount > 100 ? "中" : "低");
        return new SupplyChainSummaryDTO(networkRisk, highRiskCount, mediumRiskCount, lowRiskCount);
    }

    public PaginatedResponseDto<CompanyListDTO> getCompanies(String keyword, Pageable pageable) {
        Page<CompanyInfo> companyPage;
        if (StringUtils.hasText(keyword)) {
            companyPage = companyInfoRepository.findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            companyPage = companyInfoRepository.findAll(pageable);
        }

        Page<CompanyListDTO> dtoPage = companyPage.map(this::mapToCompanyListDTO);
        return new PaginatedResponseDto<>(dtoPage);
    }

    public CompanyInfo getCompanyById(Long id) {
        return companyInfoRepository.findById(id).orElse(null);
    }

    public CompanyInfo createCompany(CompanyInfo companyInfo) {
        // In a real app, we might want to use a DTO and map fields, but for now this is fine.
        return companyInfoRepository.save(companyInfo);
    }

    public CompanyInfo updateCompany(Long id, CompanyInfo companyDetails) {
        return companyInfoRepository.findById(id).map(company -> {
            company.setName(companyDetails.getName());
            company.setIndustry(companyDetails.getIndustry());
            // Note: In a real application, risk levels (tech, finance, etc.) and reasons
            // should be recalculated based on new data, not set directly.
            // For this implementation, we assume the provided details are what's intended to be saved.
            // We will only map the basic, non-derived fields.
            company.setMajorProduct1(companyDetails.getMajorProduct1());
            company.setMajorProduct2(companyDetails.getMajorProduct2());
            company.setRegisteredAddress(companyDetails.getRegisteredAddress());
            return companyInfoRepository.save(company);
        }).orElse(null);
    }

    public void deleteCompany(Long id) {
        companyInfoRepository.deleteById(id);
    }

    private CompanyListDTO mapToCompanyListDTO(CompanyInfo company) {
        String lawRisk = getLawRisk(company);
        String financeRisk = getFinanceRisk(company);
        String creditRisk = getCreditRisk(company);
        String techRisk = "低"; // Placeholder as no direct data is available

        List<String> reasons = new ArrayList<>();
        if ("高".equals(lawRisk) || "中".equals(lawRisk)) {
            reasons.add("存在 " + (company.getLegalDisputeCount() != null ? company.getLegalDisputeCount() : 0) + " 起法律诉讼");
        }
        if ("高".equals(financeRisk)) {
            reasons.add("关键财务数据缺失");
        }
        if ("中".equals(creditRisk)) {
            reasons.add("税务评级非A级");
        }

        String reason = reasons.isEmpty() ? "无明显风险" : String.join("，", reasons);

        return new CompanyListDTO(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                techRisk,
                financeRisk,
                lawRisk,
                creditRisk,
                reason
        );
    }

    private String getLawRisk(CompanyInfo company) {
        if (company.getLegalDisputeCount() == null) return "低";
        if (company.getLegalDisputeCount() > 500) return "高";
        if (company.getLegalDisputeCount() > 100) return "中";
        return "低";
    }

    private String getFinanceRisk(CompanyInfo company) {
        boolean hasRevenue = StringUtils.hasText(company.getRevenue()) && !"0".equals(company.getRevenue());
        boolean hasProfit = StringUtils.hasText(company.getProfit()) && !"0".equals(company.getProfit());
        return (hasRevenue && hasProfit) ? "低" : "高";
    }

    private String getCreditRisk(CompanyInfo company) {
        return "A".equals(company.getTaxRating()) ? "低" : "中"; // Assuming non-A is medium risk
    }
}
