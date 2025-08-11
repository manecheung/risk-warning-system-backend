package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyChainRiskDto {

    private List<Indicator> indicator;
    private List<DataEntry> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Indicator {
        private String name;
        private int max;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataEntry {
        private List<Number> value;
        private String name;
    }
}
