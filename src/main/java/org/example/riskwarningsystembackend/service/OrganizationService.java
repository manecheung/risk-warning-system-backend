package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.OrganizationCreateDTO;
import org.example.riskwarningsystembackend.dto.OrganizationTreeDTO;
import org.example.riskwarningsystembackend.dto.OrganizationUpdateDTO;
import org.example.riskwarningsystembackend.entity.Organization;
import org.example.riskwarningsystembackend.entity.User;
import org.example.riskwarningsystembackend.repository.OrganizationRepository;
import org.example.riskwarningsystembackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizationTreeDTO> getOrganizationTree() {
        List<Organization> allOrgs = organizationRepository.findAllWithManager(); // 使用自定义查询
        Map<Long, Organization> orgMap = allOrgs.stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return allOrgs.stream()
                .filter(org -> org.getParent() == null || !orgMap.containsKey(org.getParent().getId()))
                .map(rootOrg -> buildTree(rootOrg, allOrgs))
                .collect(Collectors.toList());
    }

    private OrganizationTreeDTO buildTree(Organization org, List<Organization> allOrgs) {
        long userCount = userRepository.countByOrganizationId(org.getId());
        Organization parent = org.getParent();
        String parentName = Optional.ofNullable(parent).map(Organization::getName).orElse("-");
        Long parentId = Optional.ofNullable(parent).map(Organization::getId).orElse(null);

        // 从关联的 User 对象中安全地获取 manager 信息
        String managerName = Optional.ofNullable(org.getManager()).map(User::getName).orElse("未指定");
        Long managerId = Optional.ofNullable(org.getManager()).map(User::getId).orElse(null);

        List<OrganizationTreeDTO> children = allOrgs.stream()
                .filter(child -> child.getParent() != null && org.getId().equals(child.getParent().getId()))
                .map(child -> buildTree(child, allOrgs))
                .collect(Collectors.toList());

        OrganizationTreeDTO dto = new OrganizationTreeDTO(
                org.getId(),
                org.getName(),
                parentName,
                parentId,
                managerName,
                (int) userCount,
                children.isEmpty() ? null : children
        );
        dto.setManagerId(managerId); // 设置 managerId
        return dto;
    }

    @Transactional
    public Organization createOrganization(OrganizationCreateDTO createDTO) {
        Organization org = new Organization();
        org.setName(createDTO.getName());

        if (createDTO.getParentId() != null) {
            organizationRepository.findById(createDTO.getParentId()).ifPresent(org::setParent);
        }

        if (createDTO.getManagerId() != null) {
            userRepository.findById(createDTO.getManagerId()).ifPresent(org::setManager);
        }

        return organizationRepository.save(org);
    }

    @Transactional
    public Optional<Organization> updateOrganization(Long id, OrganizationUpdateDTO updateDTO) {
        return organizationRepository.findById(id).map(org -> {
            org.setName(updateDTO.getName());

            // 更新负责人
            if (updateDTO.getManagerId() != null) {
                userRepository.findById(updateDTO.getManagerId()).ifPresent(org::setManager);
            } else {
                org.setManager(null); // 允许取消负责人
            }

            // 更新父级组织
            Long parentId = updateDTO.getParentId();
            if (parentId != null) {
                if (isCircularDependency(org, parentId)) {
                    throw new IllegalStateException("无法将组织设置为自己的子孙节点。");
                }
                organizationRepository.findById(parentId).ifPresent(org::setParent);
            } else {
                org.setParent(null);
            }

            return organizationRepository.save(org);
        });
    }

    private boolean isCircularDependency(Organization currentOrg, Long newParentId) {
        if (currentOrg.getId().equals(newParentId)) {
            return true;
        }
        // 递归检查
        Organization parent = organizationRepository.findById(newParentId).orElse(null);
        while (parent != null) {
            if (parent.getId().equals(currentOrg.getId())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    @Transactional
    public void deleteOrganization(Long id) {
        if (organizationRepository.existsById(id)) {
            // 检查是否有子组织
            if (organizationRepository.countByParentId(id) > 0) {
                throw new IllegalStateException("无法删除：该组织下存在子组织。");
            }
            // 检查是否有用户
            if (userRepository.countByOrganizationId(id) > 0) {
                throw new IllegalStateException("无法删除：该组织下仍有用户。");
            }
            organizationRepository.deleteById(id);
        }
    }
}
