package com.example.javatest.web;

import java.time.Instant;
import java.util.Map;

import com.example.javatest.config.AppProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final AppProperties properties;

    public HealthController(AppProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "service", properties.getServiceName(),
                "status", "ok",
                "timestamp", Instant.now().toString()
        ));
    }
}
