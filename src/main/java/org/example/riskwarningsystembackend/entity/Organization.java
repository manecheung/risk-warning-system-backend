package org.example.riskwarningsystembackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 组织实体类
 * 用于表示系统中的组织结构，支持树形组织架构和用户管理
 */
@Data
@Entity
@Table(name = "organizations")
public class Organization {
    /**
     * 组织唯一标识符
     * 主键，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 组织名称
     */
    private String name;

    /**
     * 组织描述信息
     */
    private String description;

    /**
     * 组织管理者
     * 多对一关系，指向User实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    /**
     * 父级组织
     * 多对一关系，实现组织树形结构
     * 使用@JsonBackReference避免序列化循环引用
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference("org-parent")
    private Organization parent;

    /**
     * 子组织集合
     * 一对多关系，映射到子组织的parent字段
     * 使用@JsonManagedReference避免序列化循环引用
     * 排除在toString和equals/hashCode方法之外
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("org-parent")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Organization> children;

    /**
     * 组织下的用户集合
     * 一对多关系，映射到User实体的organization字段
     * 使用@JsonManagedReference避免序列化循环引用
     * 排除在toString和equals/hashCode方法之外
     */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("org-user")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users;
}

