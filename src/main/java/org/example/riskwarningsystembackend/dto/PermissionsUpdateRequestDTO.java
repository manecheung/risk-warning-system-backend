package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionsUpdateRequestDTO {
    private List<String> permissionKeys;
}
