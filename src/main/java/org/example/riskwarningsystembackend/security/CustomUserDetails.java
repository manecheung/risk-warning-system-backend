package org.example.riskwarningsystembackend.security;

import lombok.Getter;
import org.example.riskwarningsystembackend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 自定义用户详情类，实现Spring Security的UserDetails接口
 * 用于封装用户信息并提供认证和授权所需的数据
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * 构造函数，使用系统用户实体创建CustomUserDetails实例
     *
     * @param user 系统用户实体对象，包含用户的基本信息、角色和权限
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * 获取用户的所有权限集合
     * 通过用户的角色关联获取对应的权限列表
     *
     * @return 权限集合，包含用户所有角色对应的权限
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 从用户的角色和权限中构建权限集合
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getKey()))
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户的密码
     *
     * @return 用户的加密密码
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户的用户名
     *
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 判断账户是否未过期
     *
     * @return 始终返回true，表示账户永不过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 判断账户是否未锁定
     * 根据用户状态判断，状态为"锁定"时返回false
     *
     * @return 账户未锁定返回true，否则返回false
     */
    @Override
    public boolean isAccountNonLocked() {
        return !"锁定".equals(user.getStatus());
    }

    /**
     * 判断凭证是否未过期
     *
     * @return 始终返回true，表示凭证永不过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 判断账户是否启用
     * 根据用户状态判断，状态为"正常"时返回true
     *
     * @return 账户启用返回true，否则返回false
     */
    @Override
    public boolean isEnabled() {
        return "正常".equals(user.getStatus());
    }
}

