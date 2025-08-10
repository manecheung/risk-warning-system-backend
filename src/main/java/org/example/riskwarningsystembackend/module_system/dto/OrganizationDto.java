package org.example.riskwarningsystembackend.module_system.dto;

import lombok.Data;
import org.example.riskwarningsystembackend.module_system.entity.Organization;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织架构数据传输对象
 */
@Data
public class OrganizationDto {
    private Long id;
    private String name;
    private String parent;
    private String manager;
    private int userCount;
    private List<OrganizationDto> children;

    public static OrganizationDto fromEntity(Organization org) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(org.getId());
        dto.setName(org.getName());
        dto.setManager(org.getManager());
        dto.setUserCount(org.getUsers() != null ? org.getUsers().size() : 0);
        if (org.getParent() != null) {
            dto.setParent(org.getParent().getName());
        } else {
            dto.setParent("-");
        }
        if (org.getChildren() != null && !org.getChildren().isEmpty()) {
            dto.setChildren(org.getChildren().stream().map(OrganizationDto::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }
}
