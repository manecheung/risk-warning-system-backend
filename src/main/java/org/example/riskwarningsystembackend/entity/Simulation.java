package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * 仿真模拟实体类
 * 用于表示系统中的仿真模拟记录，包含仿真模拟的基本信息和创建时间
 */
@Entity
@Table(name = "simulations")
@Data
public class Simulation {

    /**
     * 仿真模拟记录的唯一标识符
     * 使用数据库自增策略生成主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 仿真模拟名称
     * 不能为空，用于标识仿真模拟的名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 仿真模拟描述
     * 可为空，用于描述仿真模拟的详细信息
     */
    private String description;

    /**
     * 记录创建时间
     * 使用Hibernate自动填充创建时间，字段不可更新
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

