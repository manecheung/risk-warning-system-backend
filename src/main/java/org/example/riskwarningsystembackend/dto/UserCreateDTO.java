package org.example.riskwarningsystembackend.dto;

import lombok.Data;

@Data
public class UserCreateDTO {
    private String username;
    private String name;
    private String password;
    private Long roleId;
    private Long organizationId;
    private String status;
}
