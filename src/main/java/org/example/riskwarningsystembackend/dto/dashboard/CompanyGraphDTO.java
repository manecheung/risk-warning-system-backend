package org.example.riskwarningsystembackend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 公司关系图数据传输对象
 * 用于封装公司之间的关系图数据，包含节点和边的信息
 */
@Data
@AllArgsConstructor
public class CompanyGraphDTO {
    private List<Node> nodes; // 节点列表
    private List<Edge> edges; // 边列表

    /**
     * 图节点数据类
     * 表示关系图中的一个节点，包含节点的基本属性信息
     */
    @Data
    @AllArgsConstructor
    public static class Node {
        private String id; // 节点ID
        private String label; // 节点标签
        private int size; // 节点大小
    }

    /**
     * 图边数据类
     * 表示关系图中的一条边，包含边的连接关系和属性信息
     */
    @Data
    @AllArgsConstructor
    public static class Edge {
        private String source; // 边的起点ID
        private String target; // 边的终点ID
        private String label; // 边的标签
        private String type; // 边的标签类型
    }
}

