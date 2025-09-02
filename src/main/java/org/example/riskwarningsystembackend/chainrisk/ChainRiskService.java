package org.example.riskwarningsystembackend.chainrisk;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChainRiskService {

    private static final double A = 0.5; // 风险传播强度系数
    private static final double B = 0.15; // 风险触发阈值系数

    public SimulationResult runSimulation(String initialAbnormalNode) throws IOException {
        List<TradeLink> tradeLinks = loadTradeData();

        Set<String> allNodeIds = new HashSet<>();
        tradeLinks.forEach(link -> {
            allNodeIds.add(link.reporterDesc());
            allNodeIds.add(link.partnerDesc());
        });

        if (!allNodeIds.contains(initialAbnormalNode)) {
            throw new IllegalArgumentException("Initial abnormal node not found in trade data.");
        }

        Map<String, SimulationNode> nodes = allNodeIds.stream()
                .collect(Collectors.toMap(id -> id, SimulationNode::new));

        // 初始化正常贸易值
        for (TradeLink link : tradeLinks) {
            if (link.reporterDesc().equals(link.partnerDesc())) continue;

            nodes.get(link.reporterDesc()).setTotalNormalImport(
                    nodes.get(link.reporterDesc()).getTotalNormalImport() + link.primaryValue()
            );
            nodes.get(link.partnerDesc()).setTotalNormalExport(
                    nodes.get(link.partnerDesc()).getTotalNormalExport() + link.primaryValue()
            );

            // 初始化当前贸易值
            nodes.get(link.reporterDesc()).getCurrentImportValue().put(link.partnerDesc(), link.primaryValue());
            nodes.get(link.partnerDesc()).getCurrentExportValue().put(link.reporterDesc(), link.primaryValue());
        }

        List<SimulationStep> log = new ArrayList<>();
        Map<String, String> finalAbnormalReasons = new HashMap<>();
        int step = 0;

        // 步骤 0: 初始状态
        log.add(captureStep(step, nodes, new HashMap<>()));
        step++;

        // 设定初始异常节点
        nodes.get(initialAbnormalNode).setStatus("异常");
        finalAbnormalReasons.put(initialAbnormalNode, "初始化用户设定");
        Map<String, String> reasonsForNextStep = new HashMap<>();
        reasonsForNextStep.put(initialAbnormalNode, "初始化用户设定");

        // 传播初始影响
        propagateRisk(nodes.get(initialAbnormalNode), nodes);

        while (true) {
            log.add(captureStep(step, nodes, reasonsForNextStep));
            step++;

            Map<String, String> newAbnormalNodesWithReason = new HashMap<>();
            for (SimulationNode node : nodes.values()) {
                if ("正常".equals(node.getStatus())) {
                    List<String> reasons = new ArrayList<>();
                    if (node.getCurrentTotalImport() < node.getTotalNormalImport() * (1 - B)) {
                        reasons.add("进口");
                    }
                    if (node.getCurrentTotalExport() < node.getTotalNormalExport() * (1 - B)) {
                        reasons.add("出口");
                    }

                    if (!reasons.isEmpty()) {
                        newAbnormalNodesWithReason.put(node.getId(), String.join("、", reasons) + "异常");
                    }
                }
            }

            if (newAbnormalNodesWithReason.isEmpty()) {
                break;
            }

            for (Map.Entry<String, String> entry : newAbnormalNodesWithReason.entrySet()) {
                SimulationNode nodeToUpdate = nodes.get(entry.getKey());
                nodeToUpdate.setStatus("异常");
                finalAbnormalReasons.put(nodeToUpdate.getId(), entry.getValue());
                propagateRisk(nodeToUpdate, nodes);
            }
            reasonsForNextStep = newAbnormalNodesWithReason;
        }

        return new SimulationResult(log, finalAbnormalReasons);
    }

    private void propagateRisk(SimulationNode abnormalNode, Map<String, SimulationNode> allNodes) {
        // 影响下游（我的进口来源国）
        for (Map.Entry<String, Double> importEntry : abnormalNode.getCurrentImportValue().entrySet()) {
            String sourceNodeId = importEntry.getKey();
            SimulationNode sourceNode = allNodes.get(sourceNodeId);
            if (sourceNode != null) {
                double originalValue = sourceNode.getCurrentExportValue().getOrDefault(abnormalNode.getId(), 0.0);
                sourceNode.getCurrentExportValue().put(abnormalNode.getId(), originalValue * (1 - A));
            }
        }

        // 影响上游（我的出口目标国）
        for (Map.Entry<String, Double> exportEntry : abnormalNode.getCurrentExportValue().entrySet()) {
            String targetNodeId = exportEntry.getKey();
            SimulationNode targetNode = allNodes.get(targetNodeId);
            if (targetNode != null) {
                double originalValue = targetNode.getCurrentImportValue().getOrDefault(abnormalNode.getId(), 0.0);
                targetNode.getCurrentImportValue().put(abnormalNode.getId(), originalValue * (1 - A));
            }
        }
    }

    private SimulationStep captureStep(int step, Map<String, SimulationNode> nodes, Map<String, String> reasons) {
        Map<String, String> states = nodes.values().stream()
                .collect(Collectors.toMap(SimulationNode::getId, SimulationNode::getStatus));

        List<Map<String, Object>> links = new ArrayList<>();
        for (SimulationNode sourceNode : nodes.values()) {
            for (Map.Entry<String, Double> entry : sourceNode.getCurrentExportValue().entrySet()) {
                String targetNodeId = entry.getKey();
                Double value = entry.getValue();
                if (value > 0) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", sourceNode.getId());
                    link.put("target", targetNodeId);
                    link.put("value", value);
                    links.add(link);
                }
            }
        }
        return new SimulationStep(step, states, reasons, links);
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0); // Reset for the next field
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString()); // Add the last field
        return fields;
    }

    private List<TradeLink> loadTradeData() throws IOException {
        List<TradeLink> tradeLinks = new ArrayList<>();
        String resourceName = "data/trade_edges.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("Resource not found: " + resourceName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            reader.readLine(); // Skip header line
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> parts = parseCsvLine(line);
                if (parts.size() == 3) {
                    String reporter = parts.get(0);
                    String partner = parts.get(1);
                    try {
                        double value = Double.parseDouble(parts.get(2));
                        tradeLinks.add(new TradeLink(reporter, partner, value));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid line (number format issue): " + line);
                    }
                }
            }
        }
        return tradeLinks;
    }
}