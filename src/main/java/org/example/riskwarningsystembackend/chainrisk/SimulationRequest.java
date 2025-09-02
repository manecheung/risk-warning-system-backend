package org.example.riskwarningsystembackend.chainrisk;

import lombok.Data;

/**
 * 产业链风险模拟请求类
 * 用于接收REST API请求中的参数，包含初始异常节点信息
 */
@Data
public class SimulationRequest {
    private String initialAbnormalNode;
}
