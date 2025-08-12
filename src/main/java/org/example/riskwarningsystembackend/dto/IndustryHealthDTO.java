package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IndustryHealthDTO {
    private List<String> categories; // 行业名称
    private List<Integer> values; // 行业健康度
}
