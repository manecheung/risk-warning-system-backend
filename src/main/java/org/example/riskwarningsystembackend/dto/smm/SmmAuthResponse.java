package org.example.riskwarningsystembackend.dto.smm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * SMM API 认证响应DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmmAuthResponse {
    private int code;
    private String msg;
    private Data data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String token;
    }
}
