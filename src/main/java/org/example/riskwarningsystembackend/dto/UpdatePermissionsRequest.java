package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdatePermissionsRequest {
    private List<String> permissionKeys;
}
