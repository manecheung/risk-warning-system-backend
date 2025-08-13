package org.example.riskwarningsystembackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PermissionDataDTO {
    private List<String> assignedKeys;
    private List<PermissionDTO> permissionTree;
}
