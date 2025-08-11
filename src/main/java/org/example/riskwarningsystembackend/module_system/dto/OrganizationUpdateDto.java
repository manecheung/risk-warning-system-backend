package org.example.riskwarningsystembackend.module_system.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 更新组织时使用的数据传输对象
 */
@Data
public class OrganizationUpdateDto {
    @NotEmpty(message = "组织名称不能为空")
    private String name;
    private String manager;
    private Long parentId; // 允许更新上级组织
}