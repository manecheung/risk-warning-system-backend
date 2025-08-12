package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyMetricDTO {
    private String title;
    private long value;
    private String icon;
}
