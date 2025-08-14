package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateDTO {
    private String name;
    private String password; // 添加密码字段
    private Set<Long> roleIds;
    private Long organizationId;
    private String status;
}