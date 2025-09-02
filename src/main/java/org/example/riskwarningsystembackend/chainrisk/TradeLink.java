package org.example.riskwarningsystembackend.chainrisk;

/**
 * 贸易链接记录类
 * 表示两个国家或经济体之间的贸易关系，包含报告方、合作伙伴和主要贸易值
 *
 * @param reporterDesc 报告方描述（通常是贸易数据的来源国）
 * @param partnerDesc 合作伙伴描述（通常是贸易数据的目标国）
 * @param primaryValue 主要贸易值（贸易金额）
 */
public record TradeLink(String reporterDesc, String partnerDesc, double primaryValue) {
}
