package com.example.javatest.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder, AppProperties properties) {
        Duration timeout = Duration.ofMillis(properties.getRequestTimeoutMs());
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
