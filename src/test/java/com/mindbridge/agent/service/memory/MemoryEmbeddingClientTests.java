package com.mindbridge.agent.service.memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindbridge.agent.config.MindBridgeProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class MemoryEmbeddingClientTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<CapturedRequest> requests = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private int responseDimensions;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handleRequest);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void knowledgeKeyDoesNotEnableMemoryEmbeddingByDefault() {
        MindBridgeProperties properties = new MindBridgeProperties();
        properties.getEmbedding().setApiKey("knowledge-only-key");
        properties.getMemory().getEmbedding().setBaseUrl(baseUrl());
        MemoryEmbeddingClient client = new ConfiguredMemoryEmbeddingClient(properties, WebClient.builder());

        assertThat(client.embed("敏感画像内容")).isEmpty();
        assertThat(requests).isEmpty();
    }

    @Test
    void explicitMemoryConfigurationUsesOnlyItsOwnKeyAndDimensions() throws Exception {
        responseDimensions = 3;
        MindBridgeProperties properties = new MindBridgeProperties();
        properties.getMemory().getEmbedding().setEnabled(true);
        properties.getMemory().getEmbedding().setBaseUrl(baseUrl());
        properties.getMemory().getEmbedding().setApiKey("memory-test-key");
        properties.getMemory().getEmbedding().setModel("private-test-model");
        properties.getMemory().getEmbedding().setDimensions(3);
        MemoryEmbeddingClient client = new ConfiguredMemoryEmbeddingClient(properties, WebClient.builder());

        assertThat(client.embed("画像内容")).hasSize(3);
        assertThat(client.modelName()).isEqualTo("private-test-model");
        assertThat(requests).singleElement().satisfies(request -> {
            assertThat(request.path()).isEqualTo("/v1/embeddings");
            assertThat(request.authorization()).isEqualTo("Bearer memory-test-key");
        });
        JsonNode body = objectMapper.readTree(requests.get(0).body());
        assertThat(body.path("model").asText()).isEqualTo("private-test-model");
        assertThat(body.path("dimensions").asInt()).isEqualTo(3);
        assertThat(body.path("input").asText()).isEqualTo("画像内容");
    }

    @Test
    void missingMemoryKeyOrWrongDimensionsDoNotProduceVectors() {
        MindBridgeProperties properties = new MindBridgeProperties();
        properties.getEmbedding().setApiKey("knowledge-only-key");
        properties.getMemory().getEmbedding().setEnabled(true);
        properties.getMemory().getEmbedding().setBaseUrl(baseUrl());
        MemoryEmbeddingClient client = new ConfiguredMemoryEmbeddingClient(properties, WebClient.builder());

        assertThat(client.embed("画像内容")).isEmpty();
        assertThat(requests).isEmpty();

        properties.getMemory().getEmbedding().setApiKey("memory-test-key");
        client = new ConfiguredMemoryEmbeddingClient(properties, WebClient.builder());
        responseDimensions = 2;
        assertThat(client.embed("画像内容")).isEmpty();
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        requests.add(new CapturedRequest(exchange.getRequestURI().getPath(),
                exchange.getRequestHeaders().getFirst("Authorization"),
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
        String values = IntStream.range(0, responseDimensions)
                .mapToObj(index -> "0.1")
                .collect(Collectors.joining(","));
        byte[] response = ("{\"data\":[{\"embedding\":[" + values + "]}]}")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private record CapturedRequest(String path, String authorization, String body) {
    }
}
