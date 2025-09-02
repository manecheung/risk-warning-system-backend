package org.example.riskwarningsystembackend.dto.smm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * SMM价格数据响应DTO
 * 用于接收和解析SMM API价格数据接口的响应数据
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmmPriceDataResponse {
    private int code;
    private String msg;
    private List<QuotaData> data;

    /**
     * 指标数据内部类
     * 包含特定指标的价格数据信息
     */
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

    /**
     * 价格点内部类
     * 表示单个日期的价格数据点
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PricePoint {
        private LocalDate date;
        private String value; // Changed to String to handle non-numeric values from API
    }
}
