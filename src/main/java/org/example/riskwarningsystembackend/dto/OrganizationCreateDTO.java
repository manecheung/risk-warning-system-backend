package org.example.riskwarningsystembackend.dto;

import lombok.Data;

@Data
public class OrganizationCreateDTO {
    private String name;
    private Long parentId;
    private String manager;
}
