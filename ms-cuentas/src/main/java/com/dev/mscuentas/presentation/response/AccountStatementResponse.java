package com.dev.mscuentas.presentation.response;

import com.dev.mscuentas.application.dto.AccountStatementRow;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/** One row of the "Estado de cuenta" report, with the exact keys of the statement's JSON example (8.6). */
public record AccountStatementResponse(
        @JsonProperty("Fecha") String fecha,
        @JsonProperty("Cliente") String cliente,
        @JsonProperty("Numero Cuenta") String numeroCuenta,
        @JsonProperty("Tipo") String tipo,
        @JsonProperty("Saldo Inicial") BigDecimal saldoInicial,
        @JsonProperty("Estado") boolean estado,
        @JsonProperty("Movimiento") BigDecimal movimiento,
        @JsonProperty("Saldo Disponible") BigDecimal saldoDisponible) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");

    public static AccountStatementResponse from(AccountStatementRow row) {
        return new AccountStatementResponse(
                row.fecha().format(DATE_FORMAT),
                row.cliente(),
                row.numeroCuenta(),
                capitalize(row.tipoCuenta().name()),
                plain(row.saldoInicial()),
                row.estado(),
                plain(row.valor()),
                plain(row.saldo()));
    }

    /** AHORROS becomes "Ahorros", as shown in the statement. */
    private static String capitalize(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    /** Drops the trailing zeros so 100.00 is shown as 100, like in the statement's example. */
    private static BigDecimal plain(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}
