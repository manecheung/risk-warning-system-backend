package org.example.riskwarningsystembackend.dto.smm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * SMM API 指标列表响应DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmmQuotaListResponse {
    private int code;
    private String msg;
    private Data data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private int total;
        @JsonProperty("current_page")
        private int currentPage;
        @JsonProperty("page_size")
        private int pageSize;
        @JsonProperty("page_count")
        private int pageCount;
        private List<QuotaInfo> data;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuotaInfo {
        @JsonProperty("quota_id")
        private String quotaId;
        @JsonProperty("quota_name")
        private String quotaName;
        private String unit;
        private String frequency;
        private String source;
        @JsonProperty("data_start")
        private String dataStart; // 接收为字符串，便于后续处理
        @JsonProperty("data_end")
        private String dataEnd; // 接收为字符串，便于后续处理
    }
}
