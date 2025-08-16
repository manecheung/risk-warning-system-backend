package org.example.riskwarningsystembackend.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组织树DTO类，用于表示组织架构的树形结构
 * 包含组织的基本信息、父子关系、管理人员信息以及子组织列表
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrganizationTreeDTO {
    private Long id; // 组织ID
    private String name; // 组织名称
    private String parent; // 父组织名称
    private Long parentId; // 父组织的ID
    private String manager; // 管理人员姓名
    private Long managerId; // 管理人员ID
    private int userCount; // 组织用户数量
    private List<OrganizationTreeDTO> children; // 子组织列表

    /**
     * 构造函数，用于创建组织树DTO对象
     *
     * @param id 组织ID
     * @param name 组织名称
     * @param parent 父组织名称
     * @param parentId 父组织ID
     * @param manager 管理人员姓名
     * @param userCount 组织用户数量
     * @param children 子组织列表
     */
    public OrganizationTreeDTO(Long id, String name, String parent, Long parentId, String manager, int userCount, List<OrganizationTreeDTO> children) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.parentId = parentId;
        this.manager = manager;
        this.userCount = userCount;
        this.children = children;
    }
}
