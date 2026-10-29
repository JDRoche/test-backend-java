package com.dev.mscuentas.integration;

import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F6 - integration test against a real PostgreSQL and RabbitMQ (see {@link AbstractIntegrationTest}).
 * The cliente reaches MS Cuentas the way it does in production: through a cliente event on the broker.
 */
class CuentaMovimientoIntegrationTest extends AbstractIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClienteSnapshotRepository snapshots;

    private RestClient http;

    @BeforeEach
    void setUp() {
        http = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();
    }

    @Test
    @DisplayName("F6: create a cuenta, register movements and verify the balance and the statement")
    void createCuentaRegisterMovimientosAndVerifyBalance() {
        String clienteId = givenActiveCliente("Marianela Montalvo");
        String numeroCuenta = randomNumeroCuenta();

        ResponseEntity<JsonNode> created = post("/api/cuentas", Map.of("numeroCuenta", numeroCuenta,
                "tipoCuenta", "CORRIENTE", "saldoInicial", 100, "clienteId", clienteId));
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertDecimal("100", created.getBody().get("currentBalance"));

        ResponseEntity<JsonNode> deposit = post("/api/movimientos",
                Map.of("numeroCuenta", numeroCuenta, "valor", 600, "fecha", "2022-02-10T10:00:00"));
        assertEquals(HttpStatus.CREATED, deposit.getStatusCode());
        assertEquals("DEPOSITO", deposit.getBody().get("tipoMovimiento").asString());
        assertDecimal("700", deposit.getBody().get("saldo"));

        ResponseEntity<JsonNode> withdrawal = post("/api/movimientos",
                Map.of("numeroCuenta", numeroCuenta, "valor", -250, "fecha", "2022-02-09T09:00:00"));
        assertEquals(HttpStatus.CREATED, withdrawal.getStatusCode());
        assertEquals("RETIRO", withdrawal.getBody().get("tipoMovimiento").asString());
        assertDecimal("450", withdrawal.getBody().get("saldo"));

        assertDecimal("450", get("/api/cuentas/" + numeroCuenta).getBody().get("currentBalance"));

        JsonNode movimientos = get("/api/movimientos?numeroCuenta=" + numeroCuenta).getBody();
        assertEquals(2, movimientos.size());
        assertDecimal("700", movimientos.get(0).get("saldo"));

        JsonNode report = get("/api/reportes?fecha=2022-02-08,2022-02-10&cliente=" + clienteId).getBody();
        assertEquals(2, report.size());
        assertEquals("10/2/2022", report.get(0).get("Fecha").asString());
        assertEquals("Marianela Montalvo", report.get(0).get("Cliente").asString());
        assertEquals("Corriente", report.get(0).get("Tipo").asString());
        assertDecimal("100", report.get(0).get("Saldo Inicial"));
        assertDecimal("600", report.get(0).get("Movimiento"));
        assertDecimal("700", report.get(0).get("Saldo Disponible"));
        assertEquals("9/2/2022", report.get(1).get("Fecha").asString());
        assertDecimal("-250", report.get(1).get("Movimiento"));
        assertDecimal("450", report.get(1).get("Saldo Disponible"));
    }

    @Test
    @DisplayName("F3: a withdrawal without balance answers 'Saldo no disponible' and changes nothing")
    void withdrawalWithoutBalanceIsRejected() {
        String clienteId = givenActiveCliente("Juan Osorio");
        String numeroCuenta = randomNumeroCuenta();
        post("/api/cuentas", Map.of("numeroCuenta", numeroCuenta, "tipoCuenta", "AHORROS",
                "saldoInicial", 100, "clienteId", clienteId));

        ResponseEntity<JsonNode> rejected = post("/api/movimientos", Map.of("numeroCuenta", numeroCuenta, "valor", -101));

        assertEquals(HttpStatus.BAD_REQUEST, rejected.getStatusCode());
        assertEquals("Saldo no disponible", rejected.getBody().get("message").asString());
        assertDecimal("100", get("/api/cuentas/" + numeroCuenta).getBody().get("currentBalance"));
        assertEquals(0, get("/api/movimientos?numeroCuenta=" + numeroCuenta).getBody().size());
    }

    @Test
    @DisplayName("Concurrent withdrawals on one cuenta never overdraw it (row lock on a real PostgreSQL)")
    void concurrentWithdrawalsNeverOverdraw() throws Exception {
        String clienteId = givenActiveCliente("Jose Lema");
        String numeroCuenta = randomNumeroCuenta();
        post("/api/cuentas", Map.of("numeroCuenta", numeroCuenta, "tipoCuenta", "AHORROS",
                "saldoInicial", 1000, "clienteId", clienteId));

        int attempts = 10;
        List<HttpStatusCode> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(attempts)) {
            List<Future<HttpStatusCode>> futures = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                futures.add(executor.submit(() ->
                        post("/api/movimientos", Map.of("numeroCuenta", numeroCuenta, "valor", -300)).getStatusCode()));
            }
            for (Future<HttpStatusCode> future : futures) {
                results.add(future.get());
            }
        }

        long accepted = results.stream().filter(s -> s.equals(HttpStatus.CREATED)).count();
        long rejected = results.stream().filter(s -> s.equals(HttpStatus.BAD_REQUEST)).count();
        assertEquals(3, accepted, "1000 allows exactly three withdrawals of 300");
        assertEquals(attempts - 3, rejected);
        assertDecimal("100", get("/api/cuentas/" + numeroCuenta).getBody().get("currentBalance"));
    }

    @Test
    @DisplayName("A cliente deactivated through an event can no longer register movements")
    void inactiveClienteCannotOperate() {
        String clienteId = givenActiveCliente("Cliente Temporal");
        String numeroCuenta = randomNumeroCuenta();
        post("/api/cuentas", Map.of("numeroCuenta", numeroCuenta, "tipoCuenta", "AHORROS",
                "saldoInicial", 100, "clienteId", clienteId));

        publish("cliente.updated", Map.of("eventId", UUID.randomUUID().toString(),
                "occurredAt", Instant.now().plusSeconds(60).toString(),
                "clienteId", clienteId, "nombre", "Cliente Temporal", "estado", false));
        await().atMost(Duration.ofSeconds(15)).until(() ->
                snapshots.findByClienteId(clienteId).map(snapshot -> !snapshot.isEstado()).orElse(false));

        ResponseEntity<JsonNode> rejected = post("/api/movimientos", Map.of("numeroCuenta", numeroCuenta, "valor", 10));

        assertEquals(HttpStatus.BAD_REQUEST, rejected.getStatusCode());
        assertTrue(rejected.getBody().get("message").asString().contains("no está activo"));
    }

    @Test
    @DisplayName("A deleted cliente keeps its cuentas and history but can no longer operate")
    void deletedClienteKeepsItsCuentasButCannotOperate() {
        String clienteId = givenActiveCliente("Cliente Eliminado");
        String numeroCuenta = randomNumeroCuenta();
        post("/api/cuentas", Map.of("numeroCuenta", numeroCuenta, "tipoCuenta", "AHORROS",
                "saldoInicial", 100, "clienteId", clienteId));
        post("/api/movimientos", Map.of("numeroCuenta", numeroCuenta, "valor", 50, "fecha", "2022-02-10T10:00:00"));

        publish("cliente.deleted", Map.of("eventId", UUID.randomUUID().toString(),
                "occurredAt", Instant.now().plusSeconds(60).toString(), "clienteId", clienteId));
        await().atMost(Duration.ofSeconds(15)).until(() ->
                snapshots.findByClienteId(clienteId).map(snapshot -> !snapshot.isEstado()).orElse(false));

        // it can no longer operate: no movements and no new cuentas
        ResponseEntity<JsonNode> movement = post("/api/movimientos", Map.of("numeroCuenta", numeroCuenta, "valor", 10));
        assertEquals(HttpStatus.BAD_REQUEST, movement.getStatusCode());
        assertTrue(movement.getBody().get("message").asString().contains("no está activo"));
        ResponseEntity<JsonNode> newCuenta = post("/api/cuentas", Map.of("numeroCuenta", randomNumeroCuenta(),
                "tipoCuenta", "AHORROS", "saldoInicial", 1, "clienteId", clienteId));
        assertEquals(HttpStatus.BAD_REQUEST, newCuenta.getStatusCode());

        // but nothing is lost: the cuenta keeps its balance and its history, and the statement still works
        JsonNode cuenta = get("/api/cuentas/" + numeroCuenta).getBody();
        assertDecimal("150", cuenta.get("currentBalance"));
        assertTrue(cuenta.get("estado").asBoolean(), "the cuentas are not deactivated automatically");
        assertEquals(1, get("/api/movimientos?numeroCuenta=" + numeroCuenta).getBody().size());
        JsonNode report = get("/api/reportes?fecha=2022-02-10&cliente=" + clienteId).getBody();
        assertEquals(1, report.size());
        assertEquals("Cliente Eliminado", report.get(0).get("Cliente").asString());
    }

    /** Publishes a "cliente created" event and waits until MS Cuentas has stored the cliente. */
    private String givenActiveCliente(String nombre) {
        String clienteId = "it-" + UUID.randomUUID().toString().substring(0, 8);
        publish("cliente.created", Map.of("eventId", UUID.randomUUID().toString(),
                "occurredAt", Instant.now().toString(),
                "clienteId", clienteId, "nombre", nombre, "estado", true));
        await().atMost(Duration.ofSeconds(15)).until(() ->
                snapshots.findByClienteId(clienteId).map(ClienteSnapshot::isEstado).orElse(false));
        return clienteId;
    }

    private void publish(String routingKey, Map<String, Object> payload) {
        rabbitTemplate.convertAndSend("cliente.events", routingKey, payload);
    }

    private ResponseEntity<JsonNode> post(String uri, Object body) {
        return http.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().toEntity(JsonNode.class);
    }

    private ResponseEntity<JsonNode> get(String uri) {
        return http.get().uri(uri).retrieve().toEntity(JsonNode.class);
    }

    private static String randomNumeroCuenta() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 999_999));
    }

    private static void assertDecimal(String expected, JsonNode actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual.decimalValue()),
                "expected " + expected + " but was " + actual);
    }
}
