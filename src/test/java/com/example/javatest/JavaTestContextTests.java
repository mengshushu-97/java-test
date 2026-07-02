package com.example.javatest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class JavaTestContextTests {

    @Test
    void contextLoads() {
        System.setProperty("SCHEDULING_ENABLED", "false");
        try {
            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(JavaTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .properties(
                            "app.heartbeat-rate-ms=3600000",
                            "app.chain-rate-ms=3600000",
                            "logging.level.root=INFO",
                            "spring.main.banner-mode=off"
                    )
                    .run()) {
                assertThat(context.containsBean("chainService")).isTrue();
                assertThat(context.containsBean("healthController")).isTrue();
                assertThat(context.containsBean("scheduledJobs")).isFalse();
            }
        } finally {
            System.clearProperty("SCHEDULING_ENABLED");
        }
    }
}
