package com.dev.mscuentas.integration;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs the Karate features (src/test/resources/karate) against the application started on a random port,
 * with the same real PostgreSQL and RabbitMQ as the other integration tests. It is part of {@code ./gradlew test}.
 */
class KarateApiTest extends AbstractIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Test
    void karateFeatures() {
        System.setProperty("baseUrl", "http://localhost:" + port);
        System.setProperty("rabbitMgmtUrl", "http://" + RABBIT.getHost() + ":" + RABBIT.getHttpPort());
        System.setProperty("rabbitUser", RABBIT.getAdminUsername());
        System.setProperty("rabbitPassword", RABBIT.getAdminPassword());

        Results results = Runner.path("classpath:karate")
                .tags("~@ignore")
                .outputCucumberJson(false)
                .reportDir("build/karate-reports")
                .parallel(1);

        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
