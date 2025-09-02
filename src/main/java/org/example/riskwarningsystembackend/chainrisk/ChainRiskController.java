package org.example.riskwarningsystembackend.chainrisk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/chain-risk")
public class ChainRiskController {

    private static final Logger logger = LoggerFactory.getLogger(ChainRiskController.class);

    private final ChainRiskService chainRiskService;

    @Autowired
    public ChainRiskController(ChainRiskService chainRiskService) {
        this.chainRiskService = chainRiskService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> runSimulation(@RequestBody SimulationRequest request) {
        if (request.getInitialAbnormalNode() == null || request.getInitialAbnormalNode().isEmpty()) {
            return ResponseEntity.badRequest().body("initialAbnormalNode is required.");
        }

        try {
            SimulationResult result = chainRiskService.runSimulation(request.getInitialAbnormalNode());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for simulation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to load trade data during simulation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load trade data.");
        }
    }
}