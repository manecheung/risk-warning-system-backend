package org.example.riskwarningsystembackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompanyGraphDTO {
    private List<Node> nodes;
    private List<Edge> edges;

    @Data
    @AllArgsConstructor
    public static class Node {
        private String id;
        private String label;
        private int size;
    }

    @Data
    @AllArgsConstructor
    public static class Edge {
        private String source;
        private String target;
        private String label;
    }
}
