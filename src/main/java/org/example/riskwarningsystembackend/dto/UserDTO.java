package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String role;
    private String organization;
    private String status;
    private LocalDateTime lastLogin;
}
