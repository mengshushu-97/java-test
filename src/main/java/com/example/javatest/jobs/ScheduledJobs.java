package com.example.javatest.jobs;

import com.example.javatest.config.AppProperties;
import com.example.javatest.service.ChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app", name = "scheduling-enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledJobs {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobs.class);

    private final ChainService chainService;
    private final AppProperties properties;

    public ScheduledJobs(ChainService chainService, AppProperties properties) {
        this.chainService = chainService;
        this.properties = properties;
    }

    @Scheduled(fixedRateString = "${app.heartbeat-rate-ms:1000}")
    public void heartbeat() {
        log.atInfo()
                .addKeyValue("event", "heartbeat")
                .log("java service heartbeat");
    }

    @Scheduled(fixedRateString = "${app.chain-rate-ms:60000}")
    public void javaNodeAgent() {
        try {
            chainService.javaNodeAgent();
            log.atInfo()
                    .addKeyValue("event", "schedule.chain.success")
                    .addKeyValue("chain", "java->node->agent")
                    .log("scheduled java->node->agent call finished");
        } catch (Exception error) {
            log.atError()
                    .addKeyValue("event", "schedule.chain.error")
                    .addKeyValue("chain", "java->node->agent")
                    .setCause(error)
                    .log("scheduled java->node->agent call failed");
        }
    }

    @Scheduled(fixedRateString = "${app.chain-rate-ms:60000}")
    public void javaAgentJava() {
        try {
            chainService.javaAgentJava();
            log.atInfo()
                    .addKeyValue("event", "schedule.chain.success")
                    .addKeyValue("chain", "java->agent->java")
                    .log("scheduled java->agent->java call finished");
        } catch (Exception error) {
            log.atError()
                    .addKeyValue("event", "schedule.chain.error")
                    .addKeyValue("chain", "java->agent->java")
                    .setCause(error)
                    .log("scheduled java->agent->java call failed");
        }
    }
}
