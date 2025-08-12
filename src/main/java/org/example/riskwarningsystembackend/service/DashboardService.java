package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.dto.*;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.ProductInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

    private final CompanyInfoRepository companyInfoRepository;
    private final ProductInfoRepository productInfoRepository;

    public DashboardService(CompanyInfoRepository companyInfoRepository, ProductInfoRepository productInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.productInfoRepository = productInfoRepository;
    }

    public List<KeyMetricDTO> getKeyMetrics() {
        // Using real data counts
        long companyCount = companyInfoRepository.count();
        long productCount = productInfoRepository.count();
        long industryCount = companyInfoRepository.countDistinctIndustry();

        return Arrays.asList(
                new KeyMetricDTO("涵盖行业数", industryCount, "M13 10V3L4 14h7v7l9-11h-7z"),
                new KeyMetricDTO("涵盖企业数", companyCount, "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M15 21v-1a6 6 0 00-5.197-5.983"),
                new KeyMetricDTO("涵盖产品数", productCount, "M21 16V8a2 2 0 00-1-1.732l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.732l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z")
        );
    }

    public List<RiskDistributionDTO> getRiskDistribution() {
        // Mock data as per API doc
        return Arrays.asList(
                new RiskDistributionDTO(18, "高风险企业"),
                new RiskDistributionDTO(45, "中风险企业"),
                new RiskDistributionDTO(120, "低风险企业")
        );
    }

    public IndustryHealthDTO getIndustryHealth() {
        // Mock data as per API doc
        return new IndustryHealthDTO(
                Arrays.asList("通信设备", "集成电路", "医疗器械", "新能源汽车", "风电产业链", "航空航天", "电子信息产业", "化学原料制品", "高端装备制造", "生物医药", "人工智能", "云计算与大数据", "现代物流", "新材料"),
                Arrays.asList(10, 40, 15, 20, 70, 25, 60, 30, 55, 65, 80, 75, 45, 50)
        );
    }

    public SupplyChainRiskDTO getSupplyChainRisk() {
        // Mock data as per API doc
        List<SupplyChainRiskDTO.Indicator> indicators = Arrays.asList(
                new SupplyChainRiskDTO.Indicator("技术风险", 100),
                new SupplyChainRiskDTO.Indicator("信用风险", 100),
                new SupplyChainRiskDTO.Indicator("法律风险", 100),
                new SupplyChainRiskDTO.Indicator("财务风险", 100),
                new SupplyChainRiskDTO.Indicator("产业链风险", 100),
                new SupplyChainRiskDTO.Indicator("舆情风险", 100)
        );
        List<SupplyChainRiskDTO.ChainData> data = Arrays.asList(
                new SupplyChainRiskDTO.ChainData(Arrays.asList(85, 90, 60, 75, 95, 70), "风电行业产业链"),
                new SupplyChainRiskDTO.ChainData(Arrays.asList(70, 65, 80, 60, 80, 88), "集成电路产业链"),
                new SupplyChainRiskDTO.ChainData(Arrays.asList(50, 75, 70, 85, 60, 80), "新能源产业")
        );
        return new SupplyChainRiskDTO(indicators, data);
    }

    public PaginatedResponseDto<RiskAnalysisDto> getRiskAnalysis(PageRequest pageRequest) {
        // Mock data as per API doc
        List<RiskAnalysisDto> records = Arrays.asList(
                new RiskAnalysisDto("哈尔滨电气集团有限公司", "高", "risk-high", "营收数据缺失"),
                new RiskAnalysisDto("东方电气集团东方电机有限公司", "低", "risk-low", "注册资本变更"),
                new RiskAnalysisDto("南京汽轮电机(集团)有限责任公司", "中", "risk-medium", "财务数据缺失"),
                new RiskAnalysisDto("上海电气集团股份有限公司", "低", "risk-low", "法人代表变更"),
                new RiskAnalysisDto("特变电工股份有限公司", "高", "risk-high", "存在多起法律诉讼"),
                new RiskAnalysisDto("新疆金风科技股份有限公司", "中", "risk-medium", "主要股东减持股份"),
                new RiskAnalysisDto("明阳智慧能源集团股份公司", "低", "risk-low", "新增对外投资"),
                new RiskAnalysisDto("中国长江三峡集团有限公司", "低", "risk-low", "经营范围变更"),
                new RiskAnalysisDto("中国核工业集团有限公司", "高", "risk-high", "子公司涉及重大安全事故"),
                new RiskAnalysisDto("国家电力投资集团有限公司", "中", "risk-medium", "海外项目投资收益未达预期")
        );

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), records.size());
        Page<RiskAnalysisDto> page = new PageImpl<>(records.subList(start, end), pageRequest, records.size());
        return new PaginatedResponseDto<>(page);
    }

    public List<RiskMapDTO> getRiskMap() {
        // Mock data as per API doc
        return Arrays.asList(
                new RiskMapDTO("东方电气", Arrays.asList(104.065735, 30.659462, 20), "低"),
                new RiskMapDTO("新疆金风科技", Arrays.asList(87.617733, 43.792818, 20), "中"),
                new RiskMapDTO("上海电气", Arrays.asList(121.473701, 31.230416, 20), "低"),
                new RiskMapDTO("哈尔滨电气", Arrays.asList(126.635446, 45.755053, 20), "高"),
                new RiskMapDTO("明阳智能", Arrays.asList(113.383331, 23.133333, 20), "中"),
                new RiskMapDTO("中国中车", Arrays.asList(116.407428, 39.904214, 20), "低"),
                new RiskMapDTO("特变电工", Arrays.asList(87.584491, 43.825633, 20), "高")
        );
    }
}
