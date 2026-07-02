package com.example.javatest.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestResponseLoggingFilterTests {

    @Test
    void logsRequestPathInputAndOutput() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            RequestResponseLoggingFilter filter = new RequestResponseLoggingFilter(new ObjectMapper());
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/node-simple");
            request.setQueryString("debug=true");
            request.setContentType("application/json");
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
            request.setContent("""
                    {"requestId":"req-log-test","password":"secret-value"}
                    """.getBytes(StandardCharsets.UTF_8));
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain(new HttpServlet() {
                @Override
                protected void service(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
                    request.getInputStream().readAllBytes();
                    response.setContentType("application/json");
                    response.getWriter().write("""
                            {"source":"java-test","token":"abc"}
                            """);
                }
            });

            filter.doFilter(request, response, chain);

            ILoggingEvent event = appender.list.stream()
                    .filter(item -> item.getFormattedMessage().equals("request completed"))
                    .findFirst()
                    .orElseThrow();
            Map<String, Object> fields = new LinkedHashMap<>();
            event.getKeyValuePairs().forEach(pair -> fields.put(pair.key, pair.value));

            assertThat(fields).containsEntry("path", "/api/node-simple");
            assertThat(fields).containsEntry("query", "debug=true");
            assertThat(fields.get("requestBody").toString()).contains("req-log-test");
            assertThat(fields.get("requestBody").toString()).contains("[REDACTED]");
            assertThat(fields.get("responseBody").toString()).contains("java-test");
            assertThat(fields.get("responseBody").toString()).contains("[REDACTED]");
        } finally {
            logger.detachAppender(appender);
        }
    }
}
