package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;

/**
 * 新增组织时使用的数据传输对象
 */
@Data
public class OrganizationCreateDto {
    private String name;
    private Long parentId; // 父组织ID，如果为null则表示顶级组织
    private String manager;
}