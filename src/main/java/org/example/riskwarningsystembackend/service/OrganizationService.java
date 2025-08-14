package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.OrganizationCreateDTO;
import org.example.riskwarningsystembackend.dto.OrganizationTreeDTO;
import org.example.riskwarningsystembackend.dto.OrganizationUpdateDTO;
import org.example.riskwarningsystembackend.entity.Organization;
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
        List<Organization> allOrgs = organizationRepository.findAll();
        Map<Long, Organization> orgMap = allOrgs.stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return allOrgs.stream()
                .filter(org -> org.getParent() == null || !orgMap.containsKey(org.getParent().getId()))
                .map(rootOrg -> buildTree(rootOrg, allOrgs))
                .collect(Collectors.toList());
    }

    private OrganizationTreeDTO buildTree(Organization org, List<Organization> allOrgs) {
        long userCount = userRepository.countByOrganizationId(org.getId());
        // 获取父节点，并从中提取名称和ID
        Organization parent = org.getParent();
        String parentName = Optional.ofNullable(parent).map(Organization::getName).orElse("-");
        Long parentId = Optional.ofNullable(parent).map(Organization::getId).orElse(null);

        List<OrganizationTreeDTO> children = allOrgs.stream()
                .filter(child -> child.getParent() != null && org.getId().equals(child.getParent().getId()))
                .map(child -> buildTree(child, allOrgs))
                .collect(Collectors.toList());

        return new OrganizationTreeDTO(
                org.getId(),
                org.getName(),
                parentName,
                parentId, // 传递 parentId
                org.getManager(),
                (int) userCount,
                children.isEmpty() ? null : children
        );
    }

    @Transactional
    public Organization createOrganization(OrganizationCreateDTO createDTO) {
        Organization org = new Organization();
        org.setName(createDTO.getName());
        if (createDTO.getParentId() != null) {
            organizationRepository.findById(createDTO.getParentId()).ifPresent(org::setParent);
        }
        org.setManager(createDTO.getManager());
        return organizationRepository.save(org);
    }

    @Transactional
    public Optional<Organization> updateOrganization(Long id, OrganizationUpdateDTO updateDTO) {
        Optional<Organization> orgOptional = organizationRepository.findById(id);
        if (orgOptional.isEmpty()) {
            return Optional.empty();
        }

        Organization org = orgOptional.get();
        org.setName(updateDTO.getName());
        org.setManager(updateDTO.getManager());

        // 处理父级组织更新
        Long parentId = updateDTO.getParentId();

        // 检查循环依赖：不能将一个组织设置为自己的子孙节点
        if (parentId != null && isCircularDependency(org, parentId)) {
            // 在实际应用中，最好抛出一个自定义的业务异常
            throw new IllegalStateException("无法将组织设置为自己的子孙节点。");
        }

        if (parentId == null) {
            org.setParent(null);
        } else {
            organizationRepository.findById(parentId).ifPresent(org::setParent);
        }

        return Optional.of(organizationRepository.save(org));
    }

    /**
     * 检查将一个组织（newParent）设置为当前组织（currentOrg）的父节点是否会产生循环依赖。
     * @param currentOrg 当前正在被修改的组织。
     * @param newParentId 新的父组织的ID。
     * @return 如果会产生循环依赖，则返回 true。
     */
    private boolean isCircularDependency(Organization currentOrg, Long newParentId) {
        if (currentOrg.getId().equals(newParentId)) {
            return true; // 不能将自己设置为父节点
        }
        // 遍历所有子孙节点，检查 newParentId 是否是其中之一
        return currentOrg.getChildren().stream()
                .anyMatch(child -> isCircularDependency(child, newParentId));
    }

    @Transactional
    public boolean deleteOrganization(Long id) {
        // This check is simplified as children are loaded lazily.
        // A custom repository method might be more efficient.
        Optional<Organization> orgOptional = organizationRepository.findById(id);
        if (orgOptional.isPresent() && !orgOptional.get().getChildren().isEmpty()) {
            // Cannot delete organization with children
            return false;
        }

        long userCount = userRepository.countByOrganizationId(id);
        if (userCount > 0) {
            // Cannot delete organization with users
            return false;
        }

        if (orgOptional.isPresent()) {
            organizationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
