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
        String parentName = Optional.ofNullable(org.getParent())
                .map(Organization::getName)
                .orElse("-");

        List<OrganizationTreeDTO> children = allOrgs.stream()
                .filter(child -> child.getParent() != null && org.getId().equals(child.getParent().getId()))
                .map(child -> buildTree(child, allOrgs))
                .collect(Collectors.toList());

        return new OrganizationTreeDTO(
                org.getId(),
                org.getName(),
                parentName,
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
        return organizationRepository.findById(id).map(org -> {
            org.setName(updateDTO.getName());
            org.setManager(updateDTO.getManager());
            // 注意：此示例不处理父级组织的更新，如果需要，可以在此添加逻辑
            return organizationRepository.save(org);
        });
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
