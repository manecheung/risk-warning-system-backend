package org.example.riskwarningsystembackend.dto.uestc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 电子科技大学图数据DTO
 * 用于接收和解析电子科技大学风险预警系统API返回的图数据结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UestcGraphDTO {
    private List<UestcNodeDTO> nodes;
    private List<UestcLinkDTO> links;
}
