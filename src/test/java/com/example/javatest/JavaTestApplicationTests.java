package com.example.javatest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.example.javatest.service.ChainService;
import com.example.javatest.web.ChainController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class JavaTestApplicationTests {

    @Test
    void dataEndpointReturnsServiceContract() {
        ChainService service = new ChainService(null, null, null);
        ChainController controller = new ChainController(service);

        ResponseEntity<Map<String, Object>> response = controller.data();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("source", "java-test");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void nodeSimpleChainReturnsExpectedShape() {
        ChainService service = new ChainService(null, null, null);
        ChainController controller = new ChainController(service);

        ResponseEntity<Map<String, Object>> response = controller.nodeSimple(Map.of("requestId", "test-request"));

        assertThat(response.getBody()).containsEntry("source", "java-test");
        assertThat(response.getBody()).containsEntry("chain", "node->java");
    }
}
