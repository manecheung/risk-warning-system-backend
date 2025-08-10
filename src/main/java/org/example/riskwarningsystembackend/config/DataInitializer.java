package org.example.riskwarningsystembackend.config;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_chain_risk.entity.CompanyRelationship;
import org.example.riskwarningsystembackend.module_chain_risk.repository.CompanyRelationshipRepository;
import org.example.riskwarningsystembackend.module_monitoring.entity.Article;
import org.example.riskwarningsystembackend.module_monitoring.repository.ArticleRepository;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.repository.CompanyRepository;
import org.example.riskwarningsystembackend.module_system.entity.Organization;
import org.example.riskwarningsystembackend.module_system.entity.Permission;
import org.example.riskwarningsystembackend.module_system.entity.Role;
import org.example.riskwarningsystembackend.module_system.entity.User;
import org.example.riskwarningsystembackend.module_system.repository.OrganizationRepository;
import org.example.riskwarningsystembackend.module_system.repository.PermissionRepository;
import org.example.riskwarningsystembackend.module_system.repository.RoleRepository;
import org.example.riskwarningsystembackend.module_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArticleRepository articleRepository;
    private final CompanyRepository companyRepository;
    private final CompanyRelationshipRepository relationshipRepository;
    private final PermissionRepository permissionRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. 创建权限
        Permission dashboardView = createPermissionIfNotFound("查看仪表盘", "dashboard:view");
        // 系统管理权限
        Permission userList = createPermissionIfNotFound("查看用户列表", "system:user:list");
        Permission userEdit = createPermissionIfNotFound("编辑用户", "system:user:edit");
        Permission roleList = createPermissionIfNotFound("查看角色列表", "system:role:list");
        Permission roleEdit = createPermissionIfNotFound("编辑角色权限", "system:role:edit");
        Permission orgList = createPermissionIfNotFound("查看组织架构", "system:org:list");
        Permission orgEdit = createPermissionIfNotFound("编辑组织架构", "system:org:edit");

        // 2. 创建角色并分配权限
        Role adminRole = createRoleIfNotFound("系统管理员", "拥有系统的全部权限", Set.of(dashboardView, userList, userEdit, roleList, roleEdit, orgList, orgEdit));
        Role analystRole = createRoleIfNotFound("风险分析师", "可以查看风险数据并进行蔓延模拟", Set.of(dashboardView, userList));
        Role userRole = createRoleIfNotFound("普通用户", "仅能查看首页看板和预警信息", Set.of(dashboardView));

        // 3. 创建组织架构
        Organization orgHead = createOrganizationIfNotFound("总部", null, "管理员");
        Organization orgRisk = createOrganizationIfNotFound("风险控制部", orgHead, "李四");
        Organization orgMarket = createOrganizationIfNotFound("市场部", orgHead, "王五");
        Organization orgRD = createOrganizationIfNotFound("研发部", orgHead, "赵六");

        // 4. 创建用户并分配角色和组织
        createUserIfNotFound("admin", "password123", "系统管理员", "正常", Set.of(adminRole), orgHead);
        createUserIfNotFound("analyst", "password123", "风险分析师", "正常", Set.of(analystRole), orgRisk);
        createUserIfNotFound("user", "password123", "普通用户", "正常", Set.of(userRole), orgMarket);
        createUserIfNotFound("dev", "password123", "开发人员", "已禁用", Set.of(userRole), orgRD);


        // 5. 创建测试资讯数据
        createArticlesIfNotFound();

        // 6. 创建供应链与图谱数据
        createCompaniesAndRelationships();
    }

    private Permission createPermissionIfNotFound(String name, String key) {
        return permissionRepository.findByPermissionKey(key).orElseGet(() -> {
            Permission p = new Permission();
            p.setName(name);
            p.setPermissionKey(key);
            return permissionRepository.save(p);
        });
    }

    private Role createRoleIfNotFound(String name, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(name);
            newRole.setDescription(description);
            return newRole;
        });
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private Organization createOrganizationIfNotFound(String name, Organization parent, String manager) {
        // 简单通过name查找，实际项目可能需要更复杂的逻辑
        return organizationRepository.findAll().stream()
                .filter(o -> o.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Organization org = new Organization();
                    org.setName(name);
                    org.setParent(parent);
                    org.setManager(manager);
                    return organizationRepository.save(org);
                });
    }

    private void createUserIfNotFound(String username, String password, String name, String status, Set<Role> roles, Organization org) {
        userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setName(name);
            newUser.setStatus(status);
            newUser.setRoles(roles);
            newUser.setOrganization(org);
            return userRepository.save(newUser);
        });
    }

    private void createCompaniesAndRelationships() {
        if (companyRepository.count() == 0) {
            Company c1 = createCompany("哈尔滨电气集团有限公司", "制造-装备产业", "低", "高", "中", "低", "营收数据缺失", 126.63, 45.75);
            Company c2 = createCompany("东方电气集团东方电机有限公司", "制造-装备产业", "低", "低", "中", "中", "注册资本变更", 104.06, 30.65);
            Company c3 = createCompany("新疆金风科技股份有限公司", "新能源产业", "高", "中", "高", "中", "涉及多起法律诉讼", 87.61, 43.79);
            Company c4 = createCompany("明阳智慧能源集团股份公司", "新能源产业", "中", "中", "低", "低", "研发投入占比下降", 113.38, 23.13);
            Company c5 = createCompany("特变电工股份有限公司", "新能源产业", "高", "高", "中", "中", "短期偿债压力较大", 87.58, 43.82);
            Company c6 = createCompany("宁德时代新能源科技股份有限公司", "新能源产业", "高", "低", "低", "低", "无明显风险", 119.3, 26.08);
            Company c7 = createCompany("比亚迪股份有限公司", "新能源产业", "高", "中", "低", "中", "资产负债率偏高", 114.05, 22.52);

            createRelationship(c2, c1, "供应", "supplier");
            createRelationship(c3, c1, "供应", "supplier");
            createRelationship(c1, c4, "销售", "customer");
            createRelationship(c1, c5, "合作", "partner");
            createRelationship(c6, c7, "供应", "supplier");
            createRelationship(c4, c7, "合作", "partner");
        }
    }

    private Company createCompany(String name, String industry, String tech, String finance, String law, String credit, String reason, Double longitude, Double latitude) {
        Company company = new Company();
        company.setName(name);
        company.setIndustry(industry);
        company.setTech(tech);
        company.setFinance(finance);
        company.setLaw(law);
        company.setCredit(credit);
        company.setReason(reason);
        company.setLongitude(longitude);
        company.setLatitude(latitude);
        return companyRepository.save(company);
    }

    private void createRelationship(Company source, Company target, String label, String type) {
        CompanyRelationship rel = new CompanyRelationship();
        rel.setSource(source);
        rel.setTarget(target);
        rel.setLabel(label);
        rel.setType(type);
        relationshipRepository.save(rel);
    }

    private void createArticlesIfNotFound() {
        if (articleRepository.count() == 0) {
            Article article1 = new Article();
            article1.setType("news");
            article1.setTitle("风电“抢装潮”退潮！华东勘测设计院发布5份行政处罚决定书");
            article1.setAuthor("北极星风力发电网");
            article1.setDate("2024.11.12");
            article1.setImage("/法规.svg");
            article1.setTags(List.of("法规", "处罚决定书"));

            Article article2 = new Article();
            article2.setType("risk");
            article2.setTitle("漳州帆船配舾工程有限公司员工坠亡");
            article2.setAuthor("北极星风力发电网");
            article2.setDate("2024.11.12");
            article2.setImage("/风险.svg");
            article2.setTags(List.of("事故", "安全"));
            article2.setRiskSource("人员坠落, 抢救无效死亡");
            article2.setNotice("《通知》显示，2024年9月4日3时10分许...[内容省略]");
            article2.setRelatedCompany("漳州帆船配舾工程有限公司");
            article2.setRelatedProduct("船舵总筒");
            article2.setContent("<h4>事故背景</h4><p>近期，安全生产监督管理部门发布了一则关于高处作业安全的紧急通报...</p>");

            articleRepository.saveAll(List.of(article1, article2));
        }
    }
}