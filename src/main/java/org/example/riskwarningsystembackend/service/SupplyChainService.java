package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.supplychain.CompanyListDTO;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.supplychain.SupplyChainSummaryDTO;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.repository.company.CompanyInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 供应链相关业务逻辑服务类。
 * 提供公司信息的增删改查、风险等级计算、分页查询等功能。
 */
@Service
public class SupplyChainService {

    private final CompanyInfoRepository companyInfoRepository;

    /**
     * 构造方法，注入公司信息数据访问层。
     *
     * @param companyInfoRepository 公司信息数据访问接口
     */
    public SupplyChainService(CompanyInfoRepository companyInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
    }

    /**
     * 获取所有公司信息列表。
     *
     * @return 所有公司信息的列表
     */
    public List<CompanyInfo> getAllCompanies() {
        return companyInfoRepository.findAll();
    }

    /**
     * 获取供应链整体风险摘要信息。
     * 根据法律纠纷数量统计高、中、低风险公司数量，并判断整体网络风险等级。
     *
     * @return 包含整体风险等级和各类风险公司数量的摘要对象
     */
    public SupplyChainSummaryDTO getSummary() {
        long highRiskCount = companyInfoRepository.countByLegalDisputeCountGreaterThanEqual(500);
        long mediumRiskCount = companyInfoRepository.countByLegalDisputeCountBetween(101, 499);
        long lowRiskCount = companyInfoRepository.countByLegalDisputeCountLessThanEqual(100);
        String networkRisk = highRiskCount > 50 ? "高" : (mediumRiskCount > 100 ? "中" : "低");
        return new SupplyChainSummaryDTO(networkRisk, highRiskCount, mediumRiskCount, lowRiskCount);
    }

    /**
     * 分页获取公司列表，支持关键词搜索。
     * 搜索字段包括公司名称和所属行业。
     *
     * @param keyword  搜索关键词（可为空）
     * @param pageable 分页参数
     * @return 分页封装后的公司列表 DTO
     */
    public PaginatedResponseDTO<CompanyListDTO> getCompanies(String keyword, Pageable pageable) {
        Page<CompanyInfo> companyPage;
        if (StringUtils.hasText(keyword)) {
            companyPage = companyInfoRepository.findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            companyPage = companyInfoRepository.findAll(pageable);
        }

        Page<CompanyListDTO> dtoPage = companyPage.map(this::mapToCompanyListDTO);
        return new PaginatedResponseDTO<>(dtoPage);
    }

    /**
     * 根据 ID 获取单个公司信息。
     *
     * @param id 公司 ID
     * @return 公司信息对象，若不存在则返回 null
     */
    public CompanyInfo getCompanyById(Long id) {
        return companyInfoRepository.findById(id).orElse(null);
    }

    /**
     * 创建新的公司信息。
     *
     * @param companyInfo 待创建的公司信息实体
     * @return 保存后的公司信息实体
     */
    public CompanyInfo createCompany(CompanyInfo companyInfo) {
        // 在实际应用中，我们可能希望使用 DTO 并进行字段映射，但目前这样处理即可。
        return companyInfoRepository.save(companyInfo);
    }

    /**
     * 更新指定 ID 的公司信息。
     * 仅更新基础字段，不重新计算风险等级。
     *
     * @param id            公司 ID
     * @param companyDetails 新的公司信息实体
     * @return 更新后的公司信息实体，若不存在则返回 null
     */
    public CompanyInfo updateCompany(Long id, CompanyInfo companyDetails) {
        return companyInfoRepository.findById(id).map(company -> {
            company.setName(companyDetails.getName());
            company.setIndustry(companyDetails.getIndustry());
            // 注意：在实际应用中，风险等级（技术、财务等）及原因
            // 应基于新数据重新计算，而非直接设置。
            // 在本实现中，我们假设所提供的信息即为需要保存的内容。
            // 我们仅映射基本的、非衍生的字段。
            company.setMajorProduct1(companyDetails.getMajorProduct1());
            company.setMajorProduct2(companyDetails.getMajorProduct2());
            company.setRegisteredAddress(companyDetails.getRegisteredAddress());
            return companyInfoRepository.save(company);
        }).orElse(null);
    }

    /**
     * 删除指定 ID 的公司信息。
     *
     * @param id 公司 ID
     */
    public void deleteCompany(Long id) {
        companyInfoRepository.deleteById(id);
    }

    /**
     * 将 CompanyInfo 实体映射为 CompanyListDTO 对象。
     * 同时根据公司信息计算法律、财务、信用等风险等级，并生成风险原因说明。
     *
     * @param company 公司信息实体
     * @return 映射后的公司列表 DTO
     */
    private CompanyListDTO mapToCompanyListDTO(CompanyInfo company) {
        String lawRisk = getLawRisk(company);
        String financeRisk = getFinanceRisk(company);
        String creditRisk = getCreditRisk(company);
        String techRisk = "低"; // 占位符，因无直接数据可用

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

    /**
     * 计算公司的法律风险等级。
     * 判断依据为法律纠纷数量。
     *
     * @param company 公司信息实体
     * @return 法律风险等级（高/中/低）
     */
    private String getLawRisk(CompanyInfo company) {
        if (company.getLegalDisputeCount() == null) return "低";
        if (company.getLegalDisputeCount() > 500) return "高";
        if (company.getLegalDisputeCount() > 100) return "中";
        return "低";
    }

    /**
     * 计算公司的财务风险等级。
     * 判断依据为营收和利润是否非空且非零。
     *
     * @param company 公司信息实体
     * @return 财务风险等级（高/低）
     */
    private String getFinanceRisk(CompanyInfo company) {
        boolean hasRevenue = StringUtils.hasText(company.getRevenue()) && !"0".equals(company.getRevenue());
        boolean hasProfit = StringUtils.hasText(company.getProfit()) && !"0".equals(company.getProfit());
        return (hasRevenue && hasProfit) ? "低" : "高";
    }

    /**
     * 计算公司的信用风险等级。
     * 判断依据为税务评级是否为 A 级。
     *
     * @param company 公司信息实体
     * @return 信用风险等级（中/低）
     */
    private String getCreditRisk(CompanyInfo company) {
        return "A".equals(company.getTaxRating()) ? "低" : "中"; // 假设非A为中等风险。
    }
}
