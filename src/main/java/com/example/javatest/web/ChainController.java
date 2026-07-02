package com.example.javatest.web;

import java.util.Map;

import com.example.javatest.service.ChainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChainController {

    private final ChainService chainService;

    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> data() {
        return ResponseEntity.ok(chainService.data());
    }

    @GetMapping("/agent/data")
    public ResponseEntity<Map<String, Object>> agentData() {
        return ResponseEntity.ok(chainService.agentData());
    }

    @PostMapping("/node-simple")
    public ResponseEntity<Map<String, Object>> nodeSimple(@RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.ok(chainService.nodeSimple(request));
    }

    @PostMapping("/node-chain")
    public ResponseEntity<Map<String, Object>> nodeChain(@RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.ok(chainService.nodeChain(request));
    }

    @PostMapping("/trigger/java-node-agent")
    public ResponseEntity<Map<String, Object>> javaNodeAgent() {
        return ResponseEntity.ok(chainService.javaNodeAgent());
    }

    @PostMapping("/trigger/java-agent-java")
    public ResponseEntity<Map<String, Object>> javaAgentJava() {
        return ResponseEntity.ok(chainService.javaAgentJava());
    }
}
