package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SupplyChainRiskDTO {
    private List<Indicator> indicator; // 指标
    private List<ChainData> data; // 数据

    @Data
    @AllArgsConstructor
    public static class Indicator {
        private String name; // 指标名称
        private int max; // 最大值
    }

    @Data
    @AllArgsConstructor
    public static class ChainData {
        private List<Integer> value; // 数据
        private String name; // 公司名称
    }
}
