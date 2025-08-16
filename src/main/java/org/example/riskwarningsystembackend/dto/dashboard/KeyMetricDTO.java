package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 关键指标数据传输对象
 * 用于封装dashboard页面显示的关键指标信息
 */
@Data
@AllArgsConstructor
public class KeyMetricDTO {
    private String title; // 标题
    private long value; // 值
    private String icon; // 图标
}

