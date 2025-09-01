package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SMM原材料指标信息实体
 */
@Entity
@Table(name = "smm_indicator", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quotaId"})
})
@Data
public class SmmIndicator implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quota_id", nullable = false, length = 50)
    private String quotaId;

    @Column(name = "quota_name", nullable = false)
    private String quotaName;

    @Column(length = 50)
    private String unit;

    @Column(length = 20)
    private String frequency;

    @Column(length = 50)
    private String source;

    @Column(name = "data_start")
    private LocalDate dataStart;

    @Column(name = "data_end")
    private LocalDate dataEnd;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
