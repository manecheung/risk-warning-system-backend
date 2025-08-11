package org.example.riskwarningsystembackend.dto.chainrisk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRecordDto {
    private int id;
    private String name;
    private String description;
    private String creator;
    private String createTime;
}
