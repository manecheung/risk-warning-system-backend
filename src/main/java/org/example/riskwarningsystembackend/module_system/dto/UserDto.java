package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.example.riskwarningsystembackend.module_system.entity.User;
import org.example.riskwarningsystembackend.module_system.entity.Role;

/**
 * 用户数据传输对象
 */
@Data
public class UserDto {
    private Long id;
    private String username;
    private String name;
    private String role;
    private String organization;
    private String status;
    private String lastLogin;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRole(user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
        if (user.getOrganization() != null) {
            dto.setOrganization(user.getOrganization().getName());
        } else {
            dto.setOrganization("");
        }
        dto.setStatus(user.getStatus());
        if (user.getLastLogin() != null) {
            dto.setLastLogin(user.getLastLogin().format(formatter));
        } else {
            dto.setLastLogin("");
        }
        return dto;
    }
}
