package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key; // 权限标识, e.g., "system:users:create"

    @Column(nullable = false)
    private String label; // 权限描述, e.g., "创建用户"

    private Long parentId; // 父权限ID，用于构建树形结构

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}