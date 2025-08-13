package org.example.riskwarningsystembackend.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String name;
    private Long roleId;
    private Long organizationId;
    private String status;
}
