package com.dev.mscuentas.application.dto;

import java.time.LocalDate;

/** Both dates are inclusive. */
public record AccountStatementQuery(String clienteId, LocalDate from, LocalDate to) {
}
