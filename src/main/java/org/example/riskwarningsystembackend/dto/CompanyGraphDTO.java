package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompanyGraphDTO {
    private List<Node> nodes; // 节点列表
    private List<Edge> edges; // 边列表

    @Data
    @AllArgsConstructor
    public static class Node {
        private String id; // 节点ID
        private String label; // 节点标签
        private int size; // 节点大小
    }

    @Data
    @AllArgsConstructor
    public static class Edge {
        private String source; // 边的起点ID
        private String target; // 边的终点ID
        private String label; // 边的标签
        private String type; // 边的标签类型
    }
}
