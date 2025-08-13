package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateDTO {
    private String name;
    private Set<Long> roleIds; // 修改: 单个ID变为ID集合
    private Long organizationId;
    private String status;
}