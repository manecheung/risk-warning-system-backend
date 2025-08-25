package org.example.riskwarningsystembackend.dto.uestc;

import lombok.Data;

@Data
public class UestcNodeDTO {
    private String id;
    private String name;
    private String category;
    private Integer symbolSize;
    private Object value;
}
