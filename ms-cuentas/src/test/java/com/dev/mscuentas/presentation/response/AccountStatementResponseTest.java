package com.dev.mscuentas.presentation.response;

import com.dev.mscuentas.application.dto.AccountStatementRow;
import com.dev.mscuentas.domain.model.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountStatementResponseTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    private JsonNode serialize(AccountStatementRow row) {
        return mapper.readTree(mapper.writeValueAsString(AccountStatementResponse.from(row)));
    }

    @Test
    @DisplayName("The first row of the statement (8.5) serializes exactly like the JSON example of 8.6")
    void matchesTheStatementJsonExample() {
        AccountStatementRow row = new AccountStatementRow(LocalDateTime.of(2022, 2, 10, 10, 0),
                "Marianela Montalvo", "225487", AccountType.CORRIENTE,
                new BigDecimal("100.00"), true, new BigDecimal("600.00"), new BigDecimal("700.00"));

        JsonNode expected = mapper.readTree("""
                {
                  "Fecha": "10/2/2022",
                  "Cliente": "Marianela Montalvo",
                  "Numero Cuenta": "225487",
                  "Tipo": "Corriente",
                  "Saldo Inicial": 100,
                  "Estado": true,
                  "Movimiento": 600,
                  "Saldo Disponible": 700
                }""");

        assertEquals(expected, serialize(row));
    }

    @Test
    @DisplayName("The second row (8.5) shows a withdrawal as a negative number and a zero balance")
    void serializesWithdrawalRow() {
        AccountStatementRow row = new AccountStatementRow(LocalDateTime.of(2022, 2, 8, 9, 30),
                "Marianela Montalvo", "496825", AccountType.AHORROS,
                new BigDecimal("540.00"), true, new BigDecimal("-540.00"), new BigDecimal("0.00"));

        JsonNode expected = mapper.readTree("""
                {
                  "Fecha": "8/2/2022",
                  "Cliente": "Marianela Montalvo",
                  "Numero Cuenta": "496825",
                  "Tipo": "Ahorros",
                  "Saldo Inicial": 540,
                  "Estado": true,
                  "Movimiento": -540,
                  "Saldo Disponible": 0
                }""");

        assertEquals(expected, serialize(row));
    }

    @Test
    @DisplayName("Amounts with cents keep their decimals")
    void keepsDecimals() {
        AccountStatementRow row = new AccountStatementRow(LocalDateTime.of(2022, 2, 8, 9, 30),
                "X", "1", AccountType.AHORROS,
                new BigDecimal("10.50"), true, new BigDecimal("0.25"), new BigDecimal("10.75"));

        JsonNode json = serialize(row);

        assertEquals(new BigDecimal("10.5"), json.get("Saldo Inicial").decimalValue());
        assertEquals(new BigDecimal("0.25"), json.get("Movimiento").decimalValue());
        assertEquals(new BigDecimal("10.75"), json.get("Saldo Disponible").decimalValue());
    }
}
