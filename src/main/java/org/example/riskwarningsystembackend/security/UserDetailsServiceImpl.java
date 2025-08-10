package org.example.riskwarningsystembackend.security;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_system.entity.User;
import org.example.riskwarningsystembackend.module_system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户详细信息服务
 * Spring Security 在进行身份验证时会使用此服务来加载用户信息。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("未找到用户: " + username));
    }
}
