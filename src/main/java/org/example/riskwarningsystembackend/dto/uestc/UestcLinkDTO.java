package org.example.riskwarningsystembackend.dto.uestc;

import lombok.Data;

/**
 * 电子科技大学链接DTO
 * 表示图结构中节点之间的连接关系
 */
@Data
public class UestcLinkDTO {
    private String source;
    private String target;
    private Integer value;
}
