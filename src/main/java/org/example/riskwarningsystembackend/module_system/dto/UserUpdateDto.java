package org.example.riskwarningsystembackend.module_system.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 更新用户时使用的数据传输对象
 */
@Data
public class UserUpdateDto {

    @NotEmpty(message = "姓名不能为空")
    private String name;

    @NotEmpty(message = "状态不能为空")
    private String status;

    private Long organizationId;

    private Set<Long> roleIds;

    // 密码是可选的，只有当用户输入新密码时才更新
    @Size(min = 8, message = "新密码长度至少为8个字符")
    private String password;
}