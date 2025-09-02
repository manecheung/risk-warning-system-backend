package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 价格数据传输对象类
 * 用于在应用各层之间传输价格数据信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDataDto {
    private LocalDate date;
    private BigDecimal value;
}
