package org.example.riskwarningsystembackend.dto.uestc;

import lombok.Data;

/**
 * 电子科技大学节点DTO
 * 表示图结构中的节点信息，包含节点的基本属性和状态
 */
@Data
public class UestcNodeDTO {
    private String id;
    private String name;
    private String category;
    private Integer symbolSize;
    private Object value;
}
