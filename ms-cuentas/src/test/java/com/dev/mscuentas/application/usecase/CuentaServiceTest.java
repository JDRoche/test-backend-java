package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.CreateCuentaCommand;
import com.dev.mscuentas.application.dto.CuentaResult;
import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.DuplicateCuentaException;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.AccountType;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.model.Cuenta;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CuentaServiceTest {

    private final CuentaRepository cuentas = mock(CuentaRepository.class);
    private final ClienteSnapshotRepository snapshots = mock(ClienteSnapshotRepository.class);
    private final CuentaService service = new CuentaService(cuentas, snapshots);

    private final CreateCuentaCommand command =
            new CreateCuentaCommand("478758", AccountType.AHORROS, new BigDecimal("2000"), "jlema");

    @BeforeEach
    void setUp() {
        when(cuentas.save(any(Cuenta.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenCliente(boolean activo) {
        when(snapshots.findByClienteId("jlema")).thenReturn(Optional.of(
                ClienteSnapshot.create("jlema", "Jose Lema", activo, Instant.now())));
    }

    @Test
    @DisplayName("Creates an active cuenta whose balance equals the saldo inicial (statement case 478758)")
    void createsCuenta() {
        givenCliente(true);

        CuentaResult result = service.create(command);

        assertEquals("478758", result.numeroCuenta());
        assertEquals(new BigDecimal("2000.00"), result.currentBalance());
        assertTrue(result.estado());
    }

    @Test
    @DisplayName("A cuenta cannot be created for an unknown cliente")
    void rejectsUnknownCliente() {
        when(snapshots.findByClienteId("jlema")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(command));
        verify(cuentas, never()).save(any());
    }

    @Test
    @DisplayName("A cuenta cannot be created for an inactive cliente")
    void rejectsInactiveCliente() {
        givenCliente(false);

        assertThrows(DomainValidationException.class, () -> service.create(command));
        verify(cuentas, never()).save(any());
    }

    @Test
    @DisplayName("A duplicated numeroCuenta is rejected")
    void rejectsDuplicateNumeroCuenta() {
        givenCliente(true);
        when(cuentas.existsByNumeroCuenta("478758")).thenReturn(true);

        assertThrows(DuplicateCuentaException.class, () -> service.create(command));
        verify(cuentas, never()).save(any());
    }
}
