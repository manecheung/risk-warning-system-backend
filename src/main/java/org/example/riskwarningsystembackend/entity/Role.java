package org.example.riskwarningsystembackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 角色实体类
 * 用于表示系统中的角色信息，包含角色的基本信息以及与用户和权限的关联关系
 */
@Data
@Entity
@Table(name = "roles")
public class Role {
    /**
     * 角色唯一标识符
     * 使用数据库自增策略生成主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述信息
     */
    private String description;

    /**
     * 关联的用户集合
     * 多对多关系，由User实体类中的roles属性维护关联关系
     * 使用懒加载策略，避免不必要的数据加载
     * JsonBackReference注解防止序列化时出现循环引用
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonBackReference("user-role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users;

    /**
     * 关联的权限集合
     * 多对多关系，通过中间表role_permissions进行关联
     * 使用急加载策略，确保权限信息能够及时加载
     * JsonManagedReference注解管理序列化时的引用关系
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonManagedReference("role-permission")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Permission> permissions;
}

