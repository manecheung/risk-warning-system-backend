package org.example.riskwarningsystembackend.config;

import org.example.riskwarningsystembackend.entity.Permission;
import org.example.riskwarningsystembackend.entity.Role;
import org.example.riskwarningsystembackend.entity.User;
import org.example.riskwarningsystembackend.repository.PermissionRepository;
import org.example.riskwarningsystembackend.repository.RoleRepository;
import org.example.riskwarningsystembackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. 创建权限
        createPermissions();

        // 2. 创建角色并分配权限
        createRolesAndAssignPermissions();

        // 3. 创建admin用户
        createAdminUser();
    }

    private void createPermissions() {
        if (permissionRepository.count() > 0) {
            return; // 权限已存在
        }

        // 定义权限结构
        Permission dashboard = createPermission("dashboard", "首页仪表盘", null);
        createPermission("dashboard:view", "查看权限", dashboard);

        Permission monitoring = createPermission("monitoring", "网络信息监测", null);
        createPermission("monitoring:view", "查看资讯", monitoring);

        Permission chainRisk = createPermission("chain-risk", "产业链风险预警", null);
        createPermission("chain-risk:view", "查看风险", chainRisk);
        createPermission("chain-risk:manage", "管理模拟", chainRisk);

        Permission supplyChain = createPermission("supply-chain", "供应链风险评估", null);
        createPermission("supply-chain:view", "查看列表", supplyChain);
        createPermission("supply-chain:manage", "编辑与删除", supplyChain);

        Permission system = createPermission("system", "系统管理", null);
        createPermission("system:users:manage", "用户管理", system);
        createPermission("system:roles:manage", "角色管理", system);
        createPermission("system:orgs:manage", "组织管理", system);
    }

    private Permission createPermission(String key, String label, Permission parent) {
        Permission p = new Permission();
        p.setKey(key);
        p.setLabel(label);
        if (parent != null) {
            p.setParentId(parent.getId());
        }
        return permissionRepository.save(p);
    }

    private void createRolesAndAssignPermissions() {
        if (roleRepository.findByName("系统管理员").isPresent()) {
            return; // 角色已存在
        }

        // 系统管理员角色
        Role adminRole = new Role();
        adminRole.setName("系统管理员");
        adminRole.setDescription("拥有系统的全部权限");
        List<Permission> allPermissions = permissionRepository.findAll();
        adminRole.setPermissions(new HashSet<>(allPermissions));
        roleRepository.save(adminRole);

        // 普通用户角色 (可选，可以创建一个仅有查看权限的角色)
        Role userRole = new Role();
        userRole.setName("普通用户");
        userRole.setDescription("仅能查看首页看板");
        Permission dashboardView = permissionRepository.findByKey("dashboard:view").orElseThrow();
        userRole.setPermissions(new HashSet<>(List.of(dashboardView)));
        roleRepository.save(userRole);
    }

    private void createAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) {
            return; // 用户已存在
        }

        Role adminRole = roleRepository.findByName("系统管理员")
                .orElseThrow(() -> new RuntimeException("Error: Admin Role not found."));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setName("系统管理员");
        admin.setRoles(Set.of(adminRole)); // 关联角色
        admin.setStatus("正常");
        admin.setLastLogin(LocalDateTime.now());
        // admin.setOrganization(...) // 可选：关联到一个默认组织

        userRepository.save(admin);
    }
}
