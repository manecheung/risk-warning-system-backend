package org.example.riskwarningsystembackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SMM原材料价格数据实体
 */
@Entity
@Table(name = "smm_price_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"indicator_id", "price_date"})
})
@Data
public class SmmPriceData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private SmmIndicator indicator;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(precision = 18, scale = 4)
    private BigDecimal value;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
