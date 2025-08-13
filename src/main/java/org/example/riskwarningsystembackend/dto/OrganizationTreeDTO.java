package org.example.riskwarningsystembackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrganizationTreeDTO {
    private Long id;
    private String name;
    private String parent;
    private String manager;
    private int userCount;
    private List<OrganizationTreeDTO> children;

    public OrganizationTreeDTO(Long id, String name, String parent, String manager, int userCount, List<OrganizationTreeDTO> children) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.manager = manager;
        this.userCount = userCount;
        this.children = children;
    }
}
