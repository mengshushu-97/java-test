package com.example.javatest.service;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.example.javatest.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChainService {

    private static final Logger log = LoggerFactory.getLogger(ChainService.class);

    private final RestTemplate restTemplate;
    private final AppProperties properties;
    private final Clock clock;

    public ChainService(RestTemplate restTemplate, AppProperties properties, Clock clock) {
        this.restTemplate = restTemplate;
        this.properties = properties == null ? new AppProperties() : properties;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public Map<String, Object> data() {
        Map<String, Object> response = baseResponse();
        response.put("value", "java-data");
        return response;
    }

    public Map<String, Object> agentData() {
        Map<String, Object> response = baseResponse();
        response.put("value", "java-agent-data");
        response.put("purpose", "agent-callback");
        return response;
    }

    public Map<String, Object> nodeSimple(Map<String, Object> request) {
        String requestId = requestId(request);
        log.atInfo()
                .addKeyValue("event", "chain.received")
                .addKeyValue("requestId", requestId)
                .addKeyValue("chain", "node->java")
                .log("node requested java simple chain");

        Map<String, Object> response = baseResponse();
        response.put("chain", "node->java");
        response.put("requestId", requestId);
        response.put("received", request);
        return response;
    }

    public Map<String, Object> nodeChain(Map<String, Object> request) {
        String requestId = requestId(request);
        log.atInfo()
                .addKeyValue("event", "chain.received")
                .addKeyValue("requestId", requestId)
                .addKeyValue("chain", "node->java->agent")
                .log("node requested java to call agent");

        Map<String, Object> agent = postForMap(properties.getAgentServiceUrl() + "/api/process", Map.of(
                "requestId", requestId,
                "source", properties.getServiceName(),
                "chain", "node->java->agent",
                "parent", request
        ));

        Map<String, Object> response = baseResponse();
        response.put("chain", "node->java->agent");
        response.put("requestId", requestId);
        response.put("received", request);
        response.put("agent", agent);
        return response;
    }

    public Map<String, Object> javaNodeAgent() {
        String requestId = UUID.randomUUID().toString();
        log.atInfo()
                .addKeyValue("event", "chain.start")
                .addKeyValue("requestId", requestId)
                .addKeyValue("chain", "java->node->agent")
                .log("java starts java->node->agent chain");

        Map<String, Object> node = postForMap(properties.getNodeServiceUrl() + "/api/java-chain", Map.of(
                "requestId", requestId,
                "source", properties.getServiceName(),
                "chain", "java->node->agent"
        ));

        Map<String, Object> response = baseResponse();
        response.put("chain", "java->node->agent");
        response.put("requestId", requestId);
        response.put("node", node);
        return response;
    }

    public Map<String, Object> javaAgentJava() {
        String requestId = UUID.randomUUID().toString();
        log.atInfo()
                .addKeyValue("event", "chain.start")
                .addKeyValue("requestId", requestId)
                .addKeyValue("chain", "java->agent->java")
                .log("java starts java->agent->java chain");

        Map<String, Object> agent = postForMap(properties.getAgentServiceUrl() + "/api/java-agent-java", Map.of(
                "requestId", requestId,
                "source", properties.getServiceName(),
                "chain", "java->agent->java"
        ));

        Map<String, Object> response = baseResponse();
        response.put("chain", "java->agent->java");
        response.put("requestId", requestId);
        response.put("agent", agent);
        return response;
    }

    private Map<String, Object> baseResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("source", properties.getServiceName());
        response.put("timestamp", Instant.now(clock).toString());
        return response;
    }

    private String requestId(Map<String, Object> request) {
        Object requestId = request == null ? null : request.get("requestId");
        return requestId == null ? UUID.randomUUID().toString() : requestId.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postForMap(String url, Map<String, Object> payload) {
        if (restTemplate == null) {
            throw new IllegalStateException("RestTemplate is not configured");
        }
        Map<String, Object> response = restTemplate.postForObject(url, payload, Map.class);
        return response == null ? Map.of() : response;
    }
}
