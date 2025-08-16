package org.example.riskwarningsystembackend.dto.organization;

import lombok.Data;

/**
 * 组织创建数据传输对象
 * 用于封装组织创建时所需的基本信息
 */
@Data
public class OrganizationCreateDTO {
    /**
     * 组织名称
     */
    private String name;

    /**
     * 父级组织ID
     */
    private Long parentId;

    /**
     * 管理员ID
     */
    private Long managerId;
}

