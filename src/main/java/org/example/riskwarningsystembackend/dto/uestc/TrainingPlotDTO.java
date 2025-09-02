package org.example.riskwarningsystembackend.dto.uestc;

import lombok.Data;

/**
 * 训练图表DTO
 * 用于表示训练过程中生成的图表信息
 */
@Data
public class TrainingPlotDTO {
    private String filename;
    private String displayName;
    private String url;
}
