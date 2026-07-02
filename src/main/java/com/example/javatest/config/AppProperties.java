package com.example.javatest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String serviceName = "java-test";
    private String env = "local";
    private String nodeServiceUrl = "http://localhost:3000";
    private String agentServiceUrl = "http://localhost:8000";
    private int requestTimeoutMs = 5000;
    private long heartbeatRateMs = 1000;
    private long chainRateMs = 60000;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getNodeServiceUrl() {
        return nodeServiceUrl;
    }

    public void setNodeServiceUrl(String nodeServiceUrl) {
        this.nodeServiceUrl = nodeServiceUrl;
    }

    public String getAgentServiceUrl() {
        return agentServiceUrl;
    }

    public void setAgentServiceUrl(String agentServiceUrl) {
        this.agentServiceUrl = agentServiceUrl;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public long getHeartbeatRateMs() {
        return heartbeatRateMs;
    }

    public void setHeartbeatRateMs(long heartbeatRateMs) {
        this.heartbeatRateMs = heartbeatRateMs;
    }

    public long getChainRateMs() {
        return chainRateMs;
    }

    public void setChainRateMs(long chainRateMs) {
        this.chainRateMs = chainRateMs;
    }
}
