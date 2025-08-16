package org.example.riskwarningsystembackend.dto.permission;

import lombok.Data;
import java.util.List;

/**
 * 权限更新请求数据传输对象
 * 用于封装权限更新请求的数据
 */
@Data
public class PermissionsUpdateRequestDTO {
    /**
     * 权限键值列表
     * 包含需要更新的权限键值字符串集合
     */
    private List<String> permissionKeys;
}
