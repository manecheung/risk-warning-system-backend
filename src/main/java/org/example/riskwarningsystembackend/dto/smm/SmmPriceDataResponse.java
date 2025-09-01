package org.example.riskwarningsystembackend.dto.smm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmmPriceDataResponse {
    private int code;
    private String msg;
    private List<QuotaData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuotaData {
        @JsonProperty("quota_id")
        private String quotaId;

        @JsonProperty("quota_name")
        private String quotaName;

        private String unit;

        private List<PricePoint> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PricePoint {
        private LocalDate date;
        private String value; // Changed to String to handle non-numeric values from API
    }
}
