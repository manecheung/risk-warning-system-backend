package org.example.riskwarningsystembackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionDTO {
    private String key;
    private String label;
    private List<PermissionDTO> children;
}
