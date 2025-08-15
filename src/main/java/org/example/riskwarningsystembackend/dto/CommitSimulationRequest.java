package org.example.riskwarningsystembackend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 提交（保存）一次模拟的请求体
 */
@Data
public class CommitSimulationRequest {
    /**
     * 模拟运行时生成的临时ID
     */
    @NotEmpty(message = "runId is required")
    private String runId;

    /**
     * 用户为本次模拟命名的名称
     */
    @NotEmpty(message = "name is required")
    private String name;

    /**
     * 用户对本次模拟的描述
     */
    private String description;
}