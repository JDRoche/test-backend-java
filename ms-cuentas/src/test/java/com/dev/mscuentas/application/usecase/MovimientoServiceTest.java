package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.MovimientoResult;
import com.dev.mscuentas.application.dto.RegisterMovimientoCommand;
import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.InsufficientBalanceException;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.AccountType;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.model.Cuenta;
import com.dev.mscuentas.domain.model.MovementType;
import com.dev.mscuentas.domain.model.Movimiento;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.CuentaRepository;
import com.dev.mscuentas.domain.port.out.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovimientoServiceTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2022, 2, 10, 10, 0);

    private final CuentaRepository cuentas = mock(CuentaRepository.class);
    private final MovimientoRepository movimientos = mock(MovimientoRepository.class);
    private final ClienteSnapshotRepository snapshots = mock(ClienteSnapshotRepository.class);
    private final MovimientoService service = new MovimientoService(cuentas, movimientos, snapshots);

    @BeforeEach
    void setUp() {
        when(movimientos.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Cuenta cuenta(String saldo) {
        return Cuenta.rehydrate(1L, "225487", AccountType.CORRIENTE, new BigDecimal(saldo),
                new BigDecimal(saldo), true, "mmontalvo");
    }

    private void givenCuentaAndCliente(String saldo, boolean clienteActivo) {
        when(cuentas.findByNumeroCuentaForUpdate("225487")).thenReturn(Optional.of(cuenta(saldo)));
        when(snapshots.findByClienteId("mmontalvo")).thenReturn(Optional.of(
                ClienteSnapshot.create("mmontalvo", "Marianela Montalvo", clienteActivo, Instant.now())));
    }

    @Test
    @DisplayName("A deposit updates the balance and registers the movement (statement case: 100 + 600 = 700)")
    void registersDeposit() {
        givenCuentaAndCliente("100", true);

        MovimientoResult result = service.register(new RegisterMovimientoCommand("225487", new BigDecimal("600"), FECHA));

        assertEquals(MovementType.DEPOSITO, result.tipoMovimiento());
        assertEquals(new BigDecimal("700.00"), result.saldo());
        ArgumentCaptor<Cuenta> saved = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentas).save(saved.capture());
        assertEquals(new BigDecimal("700.00"), saved.getValue().getCurrentBalance());
    }

    @Test
    @DisplayName("A withdrawal without balance fails with 'Saldo no disponible' and saves nothing")
    void rejectsWithdrawalWithoutBalance() {
        givenCuentaAndCliente("100", true);

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> service.register(new RegisterMovimientoCommand("225487", new BigDecimal("-101"), FECHA)));

        assertEquals("Saldo no disponible", ex.getMessage());
        verify(cuentas, never()).save(any());
        verify(movimientos, never()).save(any());
    }

    @Test
    @DisplayName("An inactive cliente cannot register movements")
    void rejectsInactiveCliente() {
        givenCuentaAndCliente("100", false);

        assertThrows(DomainValidationException.class,
                () -> service.register(new RegisterMovimientoCommand("225487", new BigDecimal("10"), FECHA)));

        verify(movimientos, never()).save(any());
    }

    @Test
    @DisplayName("An unknown cuenta is reported as not found")
    void rejectsUnknownCuenta() {
        when(cuentas.findByNumeroCuentaForUpdate("000000")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.register(new RegisterMovimientoCommand("000000", BigDecimal.TEN, FECHA)));
    }

    @Test
    @DisplayName("The cuenta is loaded with a lock, never with a plain read, when registering")
    void locksTheCuentaRow() {
        givenCuentaAndCliente("100", true);

        service.register(new RegisterMovimientoCommand("225487", BigDecimal.TEN, FECHA));

        verify(cuentas).findByNumeroCuentaForUpdate("225487");
        verify(cuentas, never()).findByNumeroCuenta(any());
    }
}
