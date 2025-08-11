package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.dashboard.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

    /**
     * 获取关键指标数据
     */
    public List<KeyMetricDto> getKeyMetrics() {
        return Arrays.asList(
                new KeyMetricDto("涵盖行业数", 1067, "M13 10V3L4 14h7v7l9-11h-7z"),
                new KeyMetricDto("涵盖企业数", 59071, "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M15 21v-1a6 6 0 00-5.197-5.983"),
                new KeyMetricDto("涵盖产品数", 12833, "M21 16V8a2 2 0 00-1-1.732l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.732l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z")
        );
    }

    /**
     * 获取风险企业分布
     */
    public List<RiskDistributionDto> getRiskDistribution() {
        return Arrays.asList(
                new RiskDistributionDto(18, "高风险企业"),
                new RiskDistributionDto(45, "中风险企业"),
                new RiskDistributionDto(120, "低风险企业")
        );
    }

    /**
     * 获取行业健康度排行
     */
    public IndustryHealthDto getIndustryHealth() {
        List<String> categories = Arrays.asList("通信设备", "集成电路", "医疗器械", "新能源汽车", "风电产业链", "航空航天", "电子信息产业", "化学原料制品", "高端装备制造", "生物医药", "人工智能", "云计算与大数据", "现代物流", "新材料");
        List<Number> values = Arrays.asList(10, 40, 15, 20, 70, 25, 60, 30, 55, 65, 80, 75, 45, 50);
        return new IndustryHealthDto(categories, values);
    }

    /**
     * 获取产业链风险分析
     */
    public SupplyChainRiskDto getSupplyChainRisk() {
        List<SupplyChainRiskDto.Indicator> indicators = Arrays.asList(
                new SupplyChainRiskDto.Indicator("技术风险", 100),
                new SupplyChainRiskDto.Indicator("信用风险", 100),
                new SupplyChainRiskDto.Indicator("法律风险", 100),
                new SupplyChainRiskDto.Indicator("财务风险", 100),
                new SupplyChainRiskDto.Indicator("产业链风险", 100),
                new SupplyChainRiskDto.Indicator("舆情风险", 100)
        );
        List<SupplyChainRiskDto.DataEntry> data = Arrays.asList(
                new SupplyChainRiskDto.DataEntry(Arrays.asList(85, 90, 60, 75, 95, 70), "风电行业产业链"),
                new SupplyChainRiskDto.DataEntry(Arrays.asList(70, 65, 80, 60, 80, 88), "集成电路产业链"),
                new SupplyChainRiskDto.DataEntry(Arrays.asList(50, 75, 70, 85, 60, 80), "新能源产业")
        );
        return new SupplyChainRiskDto(indicators, data);
    }

    /**
     * 获取最新风险分析列表
     */
    public PaginatedResponseDto<RiskAnalysisRecordDto> getRiskAnalysis(int page, int pageSize) {
        List<RiskAnalysisRecordDto> allRecords = Arrays.asList(
                new RiskAnalysisRecordDto("哈尔滨电气集团有限公司", "高", "risk-high", "营收数据缺失"),
                new RiskAnalysisRecordDto("东方电气集团东方电机有限公司", "低", "risk-low", "注册资本变更"),
                new RiskAnalysisRecordDto("南京汽轮电机(集团)有限责任公司", "中", "risk-medium", "财务数据缺失"),
                new RiskAnalysisRecordDto("上海电气集团股份有限公司", "低", "risk-low", "法人代表变更"),
                new RiskAnalysisRecordDto("特变电工股份有限公司", "高", "risk-high", "存在多起法律诉讼"),
                new RiskAnalysisRecordDto("新疆金风科技股份有限公司", "中", "risk-medium", "主要股东减持股份"),
                new RiskAnalysisRecordDto("明阳智慧能源集团股份公司", "低", "risk-low", "新增对外投资"),
                new RiskAnalysisRecordDto("中国长江三峡集团有限公司", "低", "risk-low", "经营范围变更"),
                new RiskAnalysisRecordDto("中国核工业集团有限公司", "高", "risk-high", "子公司涉及重大安全事故"),
                new RiskAnalysisRecordDto("国家电力投资集团有限公司", "中", "risk-medium", "海外项目投资收益未达预期"),
                new RiskAnalysisRecordDto("中国华能集团有限公司", "低", "risk-low", "新增对外投资"),
                new RiskAnalysisRecordDto("中国大唐集团有限公司", "中", "risk-medium", "财务数据异常"),
                new RiskAnalysisRecordDto("国家能源投资集团有限责任公司", "高", "risk-high", "涉及重大诉讼"),
                new RiskAnalysisRecordDto("中国电力建设集团有限公司", "低", "risk-low", "法人变更"),
                new RiskAnalysisRecordDto("中国能源建设集团有限公司", "中", "risk-medium", "股东减持")
        );

        int totalRecords = allRecords.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalRecords);

        List<RiskAnalysisRecordDto> pageRecords = allRecords.subList(start, end);

        return new PaginatedResponseDto<>(
                page,
                pageSize,
                totalRecords,
                totalPages,
                page > 1,
                page < totalPages,
                pageRecords
        );
    }

    /**
     * 获取风险企业地图分布
     */
    public List<RiskMapDto> getRiskMap() {
        return Arrays.asList(
                new RiskMapDto("东方电气", Arrays.asList(104.065735, 30.659462, 100), "低"),
                new RiskMapDto("新疆金风科技", Arrays.asList(87.617733, 43.792818, 80), "中"),
                new RiskMapDto("上海电气", Arrays.asList(121.473701, 31.230416, 60), "低"),
                new RiskMapDto("哈尔滨电气", Arrays.asList(126.635446, 45.755053, 50), "高"),
                new RiskMapDto("明阳智能", Arrays.asList(113.383331, 23.133333, 40), "中"),
                new RiskMapDto("中国中车", Arrays.asList(116.407428, 39.904214, 90), "低"),
                new RiskMapDto("特变电工", Arrays.asList(87.584491, 43.825633, 75), "高")
        );
    }
}
