package org.example.riskwarningsystembackend.dto.organization;

import lombok.Data;

/**
 * 组织更新数据传输对象
 * 用于封装组织信息更新时所需的数据
 */
@Data
public class OrganizationUpdateDTO {
    /**
     * 组织名称
     */
    private String name;

    /**
     * 管理员ID
     */
    private Long managerId;

    /**
     * 父级组织ID
     */
    private Long parentId; // 添加 parentId 字段
}
