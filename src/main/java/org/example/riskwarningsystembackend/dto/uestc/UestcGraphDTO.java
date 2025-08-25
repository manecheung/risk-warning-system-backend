package org.example.riskwarningsystembackend.dto.uestc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UestcGraphDTO {
    private List<UestcNodeDTO> nodes;
    private List<UestcLinkDTO> links;
}
