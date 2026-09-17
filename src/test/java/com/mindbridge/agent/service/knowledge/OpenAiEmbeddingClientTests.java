package com.mindbridge.agent.service.knowledge;

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

class OpenAiEmbeddingClientTests {

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
    void requestsDefault512DimensionsAndAcceptsMatchingResponse() throws Exception {
        responseDimensions = 512;
        OpenAiEmbeddingClient client = createClient();

        List<Double> embedding = client.embed("如何缓解考试压力");

        assertThat(embedding).hasSize(512);
        assertThat(requests).singleElement().satisfies(request -> {
            assertThat(request.method()).isEqualTo("POST");
            assertThat(request.path()).isEqualTo("/v1/embeddings");
        });
        JsonNode body = objectMapper.readTree(requests.get(0).body());
        assertThat(body.path("model").asText()).isEqualTo("text-embedding-3-small");
        assertThat(body.path("dimensions").asInt()).isEqualTo(512);
        assertThat(body.path("encoding_format").asText()).isEqualTo("float");
    }

    @Test
    void rejectsResponseWithUnexpectedDimensions() {
        responseDimensions = 511;
        OpenAiEmbeddingClient client = createClient();

        assertThat(client.embed("维度错误时应降级")).isEmpty();
    }

    @Test
    void supportsConfiguredDimensions() throws Exception {
        responseDimensions = 3;
        MindBridgeProperties properties = createProperties();
        properties.getEmbedding().setDimensions(3);
        OpenAiEmbeddingClient client = new OpenAiEmbeddingClient(properties, WebClient.builder());

        assertThat(client.embed("使用自定义维度")).hasSize(3);
        JsonNode body = objectMapper.readTree(requests.get(0).body());
        assertThat(body.path("dimensions").asInt()).isEqualTo(3);
    }

    private OpenAiEmbeddingClient createClient() {
        return new OpenAiEmbeddingClient(createProperties(), WebClient.builder());
    }

    private MindBridgeProperties createProperties() {
        MindBridgeProperties properties = new MindBridgeProperties();
        properties.getEmbedding().setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.getEmbedding().setApiKey("test-api-key");
        properties.getEmbedding().setModel("text-embedding-3-small");
        return properties;
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requests.add(new CapturedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                requestBody));

        String values = IntStream.range(0, responseDimensions)
                .mapToObj(index -> "0.1")
                .collect(Collectors.joining(","));
        byte[] responseBytes = ("{\"data\":[{\"embedding\":[" + values + "]}]}")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private record CapturedRequest(String method, String path, String body) {
    }
}
