package org.example.riskwarningsystembackend.dto.monitoring;

import lombok.Data;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.ProductNode;

import java.util.Set;

/**
 * 风险识别结果的数据传输对象 (DTO).
 * 用于封装单次新闻内容分析后的结果.
 */
@Data
public class MonitorRiskIdentificationResult {

    /**
     * 是否判定为风险新闻.
     */
    private boolean isRisk;

    /**
     * 匹配到的风险关键词集合.
     */
    private Set<String> matchedRiskKeywords;

    /**
     * 关联到的公司实体集合.
     */
    private Set<CompanyInfo> matchedCompanies;

    /**
     * 关联到的产品实体集合.
     */
    private Set<ProductNode> matchedProducts;
}