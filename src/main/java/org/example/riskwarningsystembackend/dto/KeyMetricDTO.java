package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyMetricDTO {
    private String title; // 标题
    private long value; // 值
    private String icon; // 图标
}
