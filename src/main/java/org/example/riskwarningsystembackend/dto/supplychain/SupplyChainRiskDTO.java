package org.example.riskwarningsystembackend.dto.supplychain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 供应链风险数据传输对象
 * 用于封装供应链风险相关的指标和数据信息
 */
@Data
@AllArgsConstructor
public class SupplyChainRiskDTO {
    private List<Indicator> indicator; // 指标
    private List<ChainData> data; // 数据

    /**
     * 指标内部类
     * 定义供应链风险评估的指标信息
     */
    @Data
    @AllArgsConstructor
    public static class Indicator {
        private String name; // 指标名称
        private int max; // 最大值
    }

    /**
     * 链数据内部类
     * 定义供应链中各公司的具体数据
     */
    @Data
    @AllArgsConstructor
    public static class ChainData {
        private List<Integer> value; // 数据
        private String name; // 公司名称
    }
}

