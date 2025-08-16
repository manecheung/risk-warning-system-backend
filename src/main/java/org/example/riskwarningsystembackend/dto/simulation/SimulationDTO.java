package org.example.riskwarningsystembackend.dto.simulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 仿真数据传输对象类
 * 用于定义仿真系统中各种数据结构的DTO类集合
 */
public class SimulationDTO {

    /**
     * 节点数据传输对象
     * 表示图结构中的节点信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        /**
         * 节点唯一标识符
         */
        private String id;

        /**
         * 节点名称
         */
        private String name;
    }

    /**
     * 边数据传输对象
     * 表示图结构中节点间的关系边
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        /**
         * 边的起始节点ID
         */
        private String source;

        /**
         * 边的目标节点ID
         */
        private String target;

        /**
         * 边的类型标识
         */
        private String type;
    }

    /**
     * 时间范围数据传输对象
     * 定义仿真时间的最小值和最大值范围
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {
        /**
         * 时间范围最小值
         */
        private int min;

        /**
         * 时间范围最大值
         */
        private int max;
    }

    /**
     * 拓扑响应数据传输对象
     * 包含完整的图拓扑结构信息及时间范围
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopologyResponse {
        /**
         * 节点列表
         */
        private List<Node> nodes;

        /**
         * 边列表
         */
        private List<Edge> edges;

        /**
         * 仿真时间范围
         */
        private TimeRange timeRange;
    }

    /**
     * 节点状态数据传输对象
     * 记录节点在特定时刻的状态信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeState {
        /**
         * 节点唯一标识符
         */
        private String id;

        /**
         * 节点状态值
         */
        private int state;

        /**
         * 关键风险指标得分
         */
        private double kriScore;

        /**
         * 内部影响因子
         */
        private double innerFactor;
    }

    /**
     * 步骤响应数据传输对象
     * 包含仿真过程中某一步的所有状态信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepResponse {
        /**
         * 当前时间步
         */
        private int time;

        /**
         * 所有节点的状态列表
         */
        private List<NodeState> nodesState;
    }
}

