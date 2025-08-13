package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private List<String> roles; // 修改: 单个角色变为角色列表
    private String organization;
    private String status;
    private LocalDateTime lastLogin;
}