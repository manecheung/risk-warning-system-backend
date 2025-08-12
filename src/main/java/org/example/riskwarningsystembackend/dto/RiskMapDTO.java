package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RiskMapDTO {
    private String name;
    private List<Object> value;
    private String risk;
}
