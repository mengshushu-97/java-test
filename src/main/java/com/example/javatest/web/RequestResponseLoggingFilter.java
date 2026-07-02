package com.example.javatest.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final int MAX_LENGTH = 4096;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "authorization",
            "access_token",
            "accessToken",
            "apiKey",
            "api_key",
            "key",
            "password",
            "passwd",
            "refresh_token",
            "refreshToken",
            "secret",
            "token"
    );

    private final ObjectMapper objectMapper;

    public RequestResponseLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.atInfo()
                    .addKeyValue("event", "http.request")
                    .addKeyValue("method", request.getMethod())
                    .addKeyValue("path", request.getRequestURI())
                    .addKeyValue("query", request.getQueryString())
                    .addKeyValue("requestBody", bodyFromRequest(wrappedRequest))
                    .addKeyValue("responseBody", bodyFromResponse(wrappedResponse))
                    .addKeyValue("statusCode", wrappedResponse.getStatus())
                    .addKeyValue("durationMs", durationMs)
                    .log("request completed");
            wrappedResponse.copyBodyToResponse();
        }
    }

    private Object bodyFromRequest(ContentCachingRequestWrapper request) {
        byte[] bytes = request.getContentAsByteArray();
        if (bytes.length == 0) {
            return null;
        }
        return sanitize(payloadAsString(bytes, charsetFrom(request.getCharacterEncoding())));
    }

    private Object bodyFromResponse(ContentCachingResponseWrapper response) {
        byte[] bytes = response.getContentAsByteArray();
        if (bytes.length == 0) {
            return null;
        }
        return sanitize(payloadAsString(bytes, charsetFrom(response.getCharacterEncoding())));
    }

    private Charset charsetFrom(String value) {
        if (!StringUtils.hasText(value)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(value);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    private Object sanitize(String payload) {
        String trimmed = payload == null ? "" : payload.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("{")) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(trimmed, new TypeReference<>() {
                });
                return sanitizeMap(parsed, 0);
            } catch (Exception ignored) {
                return truncate(payload);
            }
        }
        return truncate(payload);
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeValue(String key, Object value, int depth) {
        if (isSensitiveKey(key)) {
            return "[REDACTED]";
        }
        if (value == null) {
            return null;
        }
        if (depth > 8) {
            return "[MAX_DEPTH]";
        }
        if (value instanceof Map<?, ?> map) {
            return sanitizeMap((Map<String, Object>) map, depth + 1);
        }
        if (value instanceof Iterable<?> iterable) {
            java.util.List<Object> result = new java.util.ArrayList<>();
            for (Object item : iterable) {
                result.add(sanitizeValue("", item, depth + 1));
            }
            return result;
        }
        if (value instanceof String text) {
            return truncate(text);
        }
        return value;
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> source, int depth) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            result.put(entry.getKey(), sanitizeValue(entry.getKey(), entry.getValue(), depth));
        }
        return result;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(key)
                || normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("authorization")
                || normalized.matches(".*api[-_]?key.*");
    }

    private String payloadAsString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LENGTH) + "...[TRUNCATED]";
    }
}
