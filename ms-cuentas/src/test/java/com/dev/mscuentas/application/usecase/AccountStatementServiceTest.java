package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.AccountStatementQuery;
import com.dev.mscuentas.application.dto.AccountStatementRow;
import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.AccountType;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.model.StatementLine;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.StatementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountStatementServiceTest {

    private final ClienteSnapshotRepository snapshots = mock(ClienteSnapshotRepository.class);
    private final StatementRepository statements = mock(StatementRepository.class);
    private final AccountStatementService service = new AccountStatementService(snapshots, statements);

    private void givenMarianela() {
        when(snapshots.findByClienteId("mmontalvo")).thenReturn(Optional.of(
                ClienteSnapshot.create("mmontalvo", "Marianela Montalvo", true, Instant.now())));
    }

    @Test
    @DisplayName("Returns one row per movement with the cliente name (statement 8.5)")
    void returnsRowsWithClienteName() {
        givenMarianela();
        StatementLine line = new StatementLine(LocalDateTime.of(2022, 2, 10, 10, 0), "225487",
                AccountType.CORRIENTE, new BigDecimal("100.00"), true, new BigDecimal("600.00"), new BigDecimal("700.00"));
        when(statements.findLines(any(), any(), any())).thenReturn(List.of(line));

        List<AccountStatementRow> rows = service.getStatement(
                new AccountStatementQuery("mmontalvo", LocalDate.of(2022, 2, 8), LocalDate.of(2022, 2, 10)));

        assertEquals(1, rows.size());
        assertEquals("Marianela Montalvo", rows.get(0).cliente());
        assertEquals("225487", rows.get(0).numeroCuenta());
        assertEquals(new BigDecimal("700.00"), rows.get(0).saldo());
    }

    @Test
    @DisplayName("The end date is inclusive: the query goes up to the start of the next day")
    void endDateIsInclusive() {
        givenMarianela();
        when(statements.findLines(any(), any(), any())).thenReturn(List.of());

        service.getStatement(new AccountStatementQuery("mmontalvo", LocalDate.of(2022, 2, 8), LocalDate.of(2022, 2, 10)));

        verify(statements).findLines("mmontalvo", LocalDateTime.of(2022, 2, 8, 0, 0), LocalDateTime.of(2022, 2, 11, 0, 0));
    }

    @Test
    @DisplayName("A cliente without movements in the range gets an empty report, not an error")
    void emptyReport() {
        givenMarianela();
        when(statements.findLines(any(), any(), any())).thenReturn(List.of());

        assertTrue(service.getStatement(new AccountStatementQuery("mmontalvo",
                LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2))).isEmpty());
    }

    @Test
    @DisplayName("An unknown cliente is reported as not found")
    void unknownCliente() {
        when(snapshots.findByClienteId("nadie")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getStatement(
                new AccountStatementQuery("nadie", LocalDate.of(2022, 2, 8), LocalDate.of(2022, 2, 10))));
        verify(statements, never()).findLines(any(), any(), any());
    }

    @Test
    @DisplayName("A range whose start is after its end is rejected")
    void reversedRange() {
        assertThrows(DomainValidationException.class, () -> service.getStatement(
                new AccountStatementQuery("mmontalvo", LocalDate.of(2022, 2, 10), LocalDate.of(2022, 2, 8))));
    }

    @Test
    @DisplayName("The cliente is required")
    void clienteRequired() {
        assertThrows(DomainValidationException.class, () -> service.getStatement(
                new AccountStatementQuery(" ", LocalDate.of(2022, 2, 8), LocalDate.of(2022, 2, 10))));
    }
}
