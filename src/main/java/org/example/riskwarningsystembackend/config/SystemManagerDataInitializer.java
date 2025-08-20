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

/**
 * 数据初始化类，用于在应用启动时初始化权限、角色和管理员用户。
 * 实现了Spring Boot的CommandLineRunner接口，在应用启动完成后执行初始化逻辑。
 */
@Component
public class SystemManagerDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数，注入所需的Repository和PasswordEncoder依赖。
     *
     * @param userRepository       用户数据访问对象
     * @param roleRepository       角色数据访问对象
     * @param permissionRepository 权限数据访问对象
     * @param passwordEncoder      密码编码器，用于加密用户密码
     */
    public SystemManagerDataInitializer(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 应用启动后执行的初始化方法。
     * 按顺序创建权限、角色并分配权限、创建管理员用户。
     *
     * @param args 启动参数（未使用）
     */
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

    /**
     * 创建系统所需的基础权限结构。
     * 包括首页仪表盘、网络信息监测、产业链风险预警、供应链风险评估和系统管理等模块的权限。
     * 如果已有权限存在，则跳过创建。
     */
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

    /**
     * 创建单个权限对象，并保存到数据库中。
     *
     * @param key    权限标识符，如 "dashboard:view"
     * @param label  权限显示名称，如 "查看权限"
     * @param parent 父级权限对象，用于构建权限树结构
     * @return 保存后的权限实体对象
     */
    private Permission createPermission(String key, String label, Permission parent) {
        Permission p = new Permission();
        p.setKey(key);
        p.setLabel(label);
        if (parent != null) {
            p.setParentId(parent.getId());
        }
        return permissionRepository.save(p);
    }

    /**
     * 创建系统角色并为其分配相应的权限。
     * 包括系统管理员角色（拥有全部权限）和普通用户角色（仅首页看板查看权限）。
     * 如果角色已存在，则跳过创建。
     */
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

        // 普通用户角色
        Role userRole = new Role();
        userRole.setName("普通用户");
        userRole.setDescription("普通用户默认拥有除了系统管理的所有页面的访问权限。");
        List<Permission> allPermissionsForUser = permissionRepository.findAll();
        allPermissionsForUser.removeIf(p -> p.getKey().startsWith("system"));
        userRole.setPermissions(new HashSet<>(allPermissionsForUser));
        roleRepository.save(userRole);
    }

    /**
     * 创建系统管理员用户。
     * 使用默认用户名 "admin" 和密码 "password123"（经过加密处理）。
     * 如果用户已存在，则跳过创建。
     */
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
