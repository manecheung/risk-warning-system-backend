package org.example.riskwarningsystembackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户实体类，用于表示系统中的用户信息
 * 包含用户的基本信息、组织关系、角色权限等
 */
@Data
@Entity
@Table(name = "users")
public class User {
    /**
     * 用户唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，用于登录系统
     */
    private String username;

    /**
     * 用户密码，经过加密存储
     */
    private String password;

    /**
     * 用户真实姓名
     */
    private String name;

    /**
     * 用户邮箱地址
     */
    private String email;

    /**
     * 用户联系电话
     */
    private String phone;

    /**
     * 用户是否启用状态
     */
    private boolean enabled;

    /**
     * 用户状态信息
     */
    private String status;

    /**
     * 用户最后登录时间
     */
    private LocalDateTime lastLogin;


    /**
     * 用户所属的组织机构
     * 使用懒加载避免循环引用
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonBackReference("org-user")
    private Organization organization;

    /**
     * 用户拥有的角色集合
     * 使用急加载确保权限信息及时可用
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonManagedReference("user-role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles;

    /**
     * 用户管理的组织机构集合
     * 一个用户可以管理多个组织
     */
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Organization> managedOrganizations;
}

