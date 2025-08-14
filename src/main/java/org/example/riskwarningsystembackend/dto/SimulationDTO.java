package org.example.riskwarningsystembackend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SimulationDTO {

    @NotBlank(message = "模拟名称不能为空")
    private String name;

    private String description;

    // We accept a flexible structure for nodes and edges.
    // The service layer will handle serialization to a JSON string.
    @NotEmpty(message = "节点列表不能为空")
    private List<JsonNode> nodes;

    @NotEmpty(message = "边列表不能为空")
    private List<JsonNode> edges;

    private List<List<String>> riskPath;

    // Creator will be set from the authenticated user principal in the service layer
}
