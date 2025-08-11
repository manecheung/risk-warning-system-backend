package org.example.riskwarningsystembackend.module_system.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 创建用户时使用的数据传输对象
 */
@Data
public class UserCreateDto {

    @NotEmpty(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    private String username;

    @NotEmpty(message = "姓名不能为空")
    private String name;

    @NotEmpty(message = "密码不能为空")
    @Size(min = 8, message = "密码长度至少为8个字符")
    private String password;

    @NotNull(message = "必须指定角色")
    @NotEmpty(message = "必须至少选择一个角色")
    private Set<Long> roleIds;

    @NotNull(message = "必须指定组织")
    private Long organizationId;

    @NotEmpty(message = "状态不能为空")
    private String status;
}