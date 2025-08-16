package org.example.riskwarningsystembackend.dto.simulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationInfoDTO {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
}
