package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SupplyChainRiskDTO {
    private List<Indicator> indicator;
    private List<ChainData> data;

    @Data
    @AllArgsConstructor
    public static class Indicator {
        private String name;
        private int max;
    }

    @Data
    @AllArgsConstructor
    public static class ChainData {
        private List<Integer> value;
        private String name;
    }
}
