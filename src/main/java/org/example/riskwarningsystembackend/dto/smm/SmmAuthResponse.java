package org.example.riskwarningsystembackend.dto.smm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * SMM API 认证响应DTO
 * 用于接收和解析SMM API认证接口的响应数据
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmmAuthResponse {
    private int code;
    private String msg;
    private Data data;

    /**
     * 认证响应数据内部类
     * 包含认证成功后返回的令牌信息
     */
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String token;
    }
}
