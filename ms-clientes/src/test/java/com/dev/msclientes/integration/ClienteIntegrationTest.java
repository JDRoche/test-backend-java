package com.dev.msclientes.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test against a real PostgreSQL and RabbitMQ: it checks the JOINED inheritance in the tables
 * created by BaseDatos.sql, the BCrypt hashing, and that the events reach the broker only after the commit.
 */
class ClienteIntegrationTest extends AbstractIntegrationTest {

    private static final long WAIT_MS = 5_000;

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TopicExchange clienteEventsExchange;

    @Autowired
    private JdbcTemplate jdbc;

    private final JsonMapper json = JsonMapper.builder().build();
    private RestClient http;
    private String queue;

    @BeforeEach
    void setUp() {
        http = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();
        // a private queue bound to the exchange plays the role of any consumer (declaring it opens the connection,
        // which declares the exchange first). It must not be auto-delete: receive(queue, timeout) uses a consumer
        // and an auto-delete queue would disappear as soon as the first receive ends.
        queue = "it-" + UUID.randomUUID().toString().substring(0, 8);
        Queue q = new Queue(queue, false, false, false);
        amqpAdmin.declareQueue(q);
        amqpAdmin.declareBinding(BindingBuilder.bind(q).to(clienteEventsExchange).with("cliente.#"));
    }

    @AfterEach
    void tearDown() {
        amqpAdmin.deleteQueue(queue);
    }

    @Test
    @DisplayName("A cliente is stored in persona + cliente (JOINED), its password is hashed and cliente.created is published")
    void createStoresJoinedRowsHashesPasswordAndPublishesEvent() throws Exception {
        String clienteId = newClienteId();
        String identificacion = newIdentificacion();

        ResponseEntity<JsonNode> created = post("/api/clientes", Map.of("clienteId", clienteId,
                "nombre", "Jose Lema", "genero", "MALE", "edad", 35, "identificacion", identificacion,
                "direccion", "Otavalo sn y principal", "telefono", "098254785", "contrasena", "1234"));

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNull(created.getBody().get("contrasena"), "the password must never be returned");

        Map<String, Object> row = jdbc.queryForMap("""
                select p.nombre, p.identificacion, p.genero, c.contrasena, c.estado
                from clientes.persona p join clientes.cliente c on c.persona_id = p.id
                where c.cliente_id = ?""", clienteId);
        assertEquals("Jose Lema", row.get("nombre"));
        assertEquals(identificacion, row.get("identificacion"));
        assertEquals("MALE", row.get("genero"));
        assertEquals(true, row.get("estado"));
        String stored = (String) row.get("contrasena");
        assertNotEquals("1234", stored);
        assertTrue(new BCryptPasswordEncoder().matches("1234", stored), "the stored value must be a BCrypt hash of 1234");

        Message message = rabbitTemplate.receive(queue, WAIT_MS);
        assertNotNull(message, "cliente.created should have been published");
        assertEquals("cliente.created", message.getMessageProperties().getReceivedRoutingKey());
        JsonNode event = json.readTree(new String(message.getBody()));
        assertEquals(clienteId, event.get("clienteId").asString());
        assertEquals("Jose Lema", event.get("nombre").asString());
        assertTrue(event.get("estado").asBoolean());
        assertNotNull(UUID.fromString(event.get("eventId").asString()));
    }

    @Test
    @DisplayName("Update and delete publish cliente.updated and cliente.deleted and remove both rows")
    void updateAndDeletePublishEvents() throws Exception {
        String clienteId = newClienteId();
        String identificacion = newIdentificacion();
        post("/api/clientes", Map.of("clienteId", clienteId, "nombre", "Juan Osorio",
                "identificacion", identificacion, "contrasena", "1245"));
        rabbitTemplate.receive(queue, WAIT_MS);

        ResponseEntity<JsonNode> updated = http.put().uri("/api/clientes/" + clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("nombre", "Juan Osorio", "identificacion", identificacion, "estado", false))
                .retrieve().toEntity(JsonNode.class);
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        Message updatedMessage = rabbitTemplate.receive(queue, WAIT_MS);
        assertNotNull(updatedMessage);
        assertEquals("cliente.updated", updatedMessage.getMessageProperties().getReceivedRoutingKey());
        assertEquals(false, json.readTree(new String(updatedMessage.getBody())).get("estado").asBoolean());

        ResponseEntity<Void> deleted = http.delete().uri("/api/clientes/" + clienteId).retrieve().toBodilessEntity();
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());
        Message deletedMessage = rabbitTemplate.receive(queue, WAIT_MS);
        assertNotNull(deletedMessage);
        assertEquals("cliente.deleted", deletedMessage.getMessageProperties().getReceivedRoutingKey());

        assertEquals(0, jdbc.queryForObject("select count(*) from clientes.cliente where cliente_id = ?",
                Integer.class, clienteId));
        assertEquals(0, jdbc.queryForObject("select count(*) from clientes.persona where identificacion = ?",
                Integer.class, identificacion));
    }

    @Test
    @DisplayName("A rejected request (rolled back / not stored) publishes no event")
    void rejectedRequestPublishesNothing() throws Exception {
        String identificacion = newIdentificacion();
        post("/api/clientes", Map.of("clienteId", newClienteId(), "nombre", "Primero",
                "identificacion", identificacion, "contrasena", "1"));
        assertNotNull(rabbitTemplate.receive(queue, WAIT_MS), "the first creation is published");

        ResponseEntity<JsonNode> duplicate = post("/api/clientes", Map.of("clienteId", newClienteId(),
                "nombre", "Duplicado", "identificacion", identificacion, "contrasena", "1"));

        assertEquals(HttpStatus.CONFLICT, duplicate.getStatusCode());
        assertNull(rabbitTemplate.receive(queue, 1_500), "no event for a request that was not stored");
    }

    private ResponseEntity<JsonNode> post(String uri, Object body) {
        return http.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().toEntity(JsonNode.class);
    }

    private static String newClienteId() {
        return "it-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String newIdentificacion() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L));
    }
}
