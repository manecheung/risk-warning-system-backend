package org.example.riskwarningsystembackend.dto;

import lombok.Data;

@Data
public class OrganizationUpdateDTO {
    private String name;
    private String manager;
    private Long parentId; // 添加 parentId 字段
}
