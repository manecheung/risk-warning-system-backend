package org.example.riskwarningsystembackend.security;

import org.example.riskwarningsystembackend.entity.User;
import org.example.riskwarningsystembackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户详细信息服务实现类
 * 实现Spring Security的UserDetailsService接口，用于加载用户详细信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 构造函数
     * @param userRepository 用户数据访问对象
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名加载用户详细信息
     * 该方法通过用户名从数据库中查找用户信息，并将其封装为UserDetails对象返回
     * @param username 用户名
     * @return UserDetails 用户详细信息对象
     * @throws UsernameNotFoundException 当用户不存在时抛出此异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查找用户信息，如果用户不存在则抛出异常
        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 将用户实体转换为自定义的用户详情对象
        return new CustomUserDetails(user);
    }
}

