package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 行业健康度数据传输对象
 * 用于封装行业健康度相关的数据信息
 */
@Data
@AllArgsConstructor
public class IndustryHealthDTO {
    private List<String> categories; // 行业名称
    private List<Integer> values; // 行业健康度
}

