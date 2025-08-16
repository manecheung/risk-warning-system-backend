package org.example.riskwarningsystembackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 权限实体类，用于表示系统中的权限信息
 * 该类映射到数据库中的permissions表，包含权限的基本信息和关联的角色信息
 */
@Data
@Entity
@Table(name = "permissions")
public class Permission {
    /**
     * 权限唯一标识符
     * 使用数据库自增策略生成主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述信息
     */
    private String description;

    /**
     * 权限对应的资源标识
     * 例如: "user:create" 表示用户创建权限
     */
    private String resource; // 权限对应的资源标识，如 "user:create"

    /**
     * 权限键值
     */
    private String key;

    /**
     * 权限标签显示名称
     */
    private String label;

    /**
     * 父级权限ID，用于构建权限树结构
     */
    private Long parentId;

    /**
     * 关联的角色集合
     * 使用多对多关系映射，该权限被哪些角色拥有
     * 采用懒加载策略，避免不必要的数据加载
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @JsonBackReference("role-permission")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles;
}
