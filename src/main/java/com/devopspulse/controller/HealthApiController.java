package com.devopspulse.controller;

import com.devopspulse.service.StatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST API endpoints used for automated health verification across the
 * DevOps lifecycle: the Dockerfile HEALTHCHECK, the Jenkins "Docker Health
 * Audit" stage, and the Azure Pipelines post-deployment verification step
 * all call GET /health. GET /api/status exposes a richer status payload
 * consumed by the SRE dashboard UI.
 */
@RestController
public class HealthApiController {

    private final StatusService statusService;

    public HealthApiController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", statusService.getApplicationName());
        body.put("version", statusService.getVersion());

        HttpStatus httpStatus = statusService.isHealthy() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        if (!statusService.isHealthy()) {
            body.put("status", "DOWN");
        }
        return ResponseEntity.status(httpStatus).body(body);
    }

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(statusService.getFullStatus());
    }
}
