package com.mindbridge.agent.service.memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindbridge.agent.config.MindBridgeProperties;
import com.mindbridge.agent.domain.UserAccount;
import com.mindbridge.agent.domain.UserMemoryItem;
import com.mindbridge.agent.domain.UserMemoryType;
import com.mindbridge.agent.service.memory.UserMemoryChromaGateway.UserMemoryMatch;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

class UserMemoryChromaGatewayTests {

    private static final String COLLECTION_ID = "22222222-2222-2222-2222-222222222222";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<CapturedRequest> requests = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private UserMemoryChromaGateway gateway;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handleRequest);
        server.start();

        MindBridgeProperties properties = new MindBridgeProperties();
        properties.getMemory().setUseChroma(true);
        properties.getMemory().setChromaBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.getMemory().setChromaCollection("mindbridge_user_memory");
        gateway = new UserMemoryChromaGateway(properties, WebClient.builder());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void usesV2CollectionIdAndKeepsUserFilterOnQuery() throws Exception {
        UserAccount user = new UserAccount();
        ReflectionTestUtils.setField(user, "id", 7L);

        UserMemoryItem item = new UserMemoryItem();
        ReflectionTestUtils.setField(item, "id", 23L);
        item.setUser(user);
        item.setType(UserMemoryType.SUPPORT_NEED);
        item.setSummary("考试前需要先梳理任务优先级");
        item.setEvidence("最近两次备考时都这样更安心");

        gateway.mirror(item);
        List<UserMemoryMatch> matches = gateway.query(7L, "最近复习压力很大", 4);
        gateway.delete(23L);

        assertThat(requests).extracting(CapturedRequest::method).containsOnly("POST");
        assertThat(requests).extracting(CapturedRequest::path).containsExactly(
                "/api/v2/tenants/default_tenant/databases/default_database/collections",
                "/api/v2/tenants/default_tenant/databases/default_database/collections/" + COLLECTION_ID + "/upsert",
                "/api/v2/tenants/default_tenant/databases/default_database/collections/" + COLLECTION_ID + "/query",
                "/api/v2/tenants/default_tenant/databases/default_database/collections/" + COLLECTION_ID + "/delete");

        JsonNode createBody = objectMapper.readTree(requests.get(0).body());
        assertThat(createBody.path("name").asText()).isEqualTo("mindbridge_user_memory");
        assertThat(createBody.path("get_or_create").asBoolean()).isTrue();

        JsonNode upsertBody = objectMapper.readTree(requests.get(1).body());
        assertThat(upsertBody.path("ids").path(0).asText()).isEqualTo("memory:23");
        assertThat(upsertBody.path("metadatas").path(0).path("memoryId").asText()).isEqualTo("23");
        assertThat(upsertBody.path("metadatas").path(0).path("userId").asText()).isEqualTo("7");

        JsonNode queryBody = objectMapper.readTree(requests.get(2).body());
        assertThat(queryBody.path("query_texts").path(0).asText()).isEqualTo("最近复习压力很大");
        assertThat(queryBody.path("n_results").asInt()).isEqualTo(4);
        assertThat(queryBody.path("where").path("userId").asText()).isEqualTo("7");

        JsonNode deleteBody = objectMapper.readTree(requests.get(3).body());
        assertThat(deleteBody.path("ids").path(0).asText()).isEqualTo("memory:23");
        assertThat(matches).singleElement().satisfies(match -> {
            assertThat(match.memoryId()).isEqualTo(23L);
            assertThat(match.score()).isEqualTo(0.85);
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requests.add(new CapturedRequest(exchange.getRequestMethod(), path, requestBody));

        String responseBody;
        if (path.endsWith("/collections")) {
            responseBody = "{\"id\":\"" + COLLECTION_ID + "\",\"name\":\"mindbridge_user_memory\"}";
        } else if (path.endsWith("/query")) {
            responseBody = "{\"ids\":[[\"memory:23\"]],\"metadatas\":[[{\"memoryId\":\"23\","
                    + "\"userId\":\"7\"}]],\"distances\":[[0.15]]}";
        } else {
            responseBody = "{}";
        }

        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private record CapturedRequest(String method, String path, String body) {
    }
}
