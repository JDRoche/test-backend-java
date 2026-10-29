package com.dev.mscuentas.domain.model;

import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuentaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2022, 2, 10, 10, 0);

    private Cuenta cuenta(String saldoInicial) {
        return Cuenta.create("478758", AccountType.AHORROS, new BigDecimal(saldoInicial), "jlema");
    }

    @ParameterizedTest(name = "saldo inicial {0} + movimiento {1} = saldo disponible {2}")
    @DisplayName("Business cases of the statement (section 8.4)")
    @CsvSource({
            "2000, -575, 1425",
            "100, 600, 700",
            "0, 150, 150",
            "540, -540, 0"
    })
    void appliesMovementsOfTheStatement(String saldoInicial, String valor, String expected) {
        Cuenta cuenta = cuenta(saldoInicial);

        Movimiento movimiento = cuenta.register(new BigDecimal(valor), FECHA);

        assertEquals(new BigDecimal(expected).setScale(2), cuenta.getCurrentBalance());
        assertEquals(new BigDecimal(expected).setScale(2), movimiento.getSaldo());
        assertEquals(new BigDecimal(valor).setScale(2), movimiento.getValor());
    }

    @Test
    @DisplayName("A positive valor is a DEPOSITO and a negative one a RETIRO")
    void derivesMovementTypeFromSign() {
        Cuenta cuenta = cuenta("100");

        assertEquals(MovementType.DEPOSITO, cuenta.register(new BigDecimal("50"), FECHA).getTipoMovimiento());
        assertEquals(MovementType.RETIRO, cuenta.register(new BigDecimal("-20"), FECHA).getTipoMovimiento());
    }

    @Test
    @DisplayName("A withdrawal larger than the balance fails with 'Saldo no disponible' and leaves the balance untouched")
    void rejectsWithdrawalWithoutBalance() {
        Cuenta cuenta = cuenta("100");

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class,
                () -> cuenta.register(new BigDecimal("-100.01"), FECHA));

        assertEquals("Saldo no disponible", ex.getMessage());
        assertEquals(new BigDecimal("100.00"), cuenta.getCurrentBalance());
    }

    @Test
    @DisplayName("Withdrawing the exact balance is allowed")
    void allowsWithdrawingTheWholeBalance() {
        Cuenta cuenta = cuenta("100");

        cuenta.register(new BigDecimal("-100"), FECHA);

        assertEquals(0, cuenta.getCurrentBalance().signum());
    }

    @Test
    @DisplayName("A movement of zero is rejected")
    void rejectsZeroMovement() {
        assertThrows(DomainValidationException.class, () -> cuenta("100").register(BigDecimal.ZERO, FECHA));
    }

    @Test
    @DisplayName("More than 2 decimals are rejected instead of silently rounded")
    void rejectsExcessDecimals() {
        assertThrows(DomainValidationException.class, () -> cuenta("100").register(new BigDecimal("1.005"), FECHA));
    }

    @Test
    @DisplayName("An inactive account does not accept movements")
    void rejectsMovementOnInactiveAccount() {
        Cuenta cuenta = cuenta("100");
        cuenta.update(AccountType.AHORROS, false);

        assertThrows(DomainValidationException.class, () -> cuenta.register(new BigDecimal("10"), FECHA));
        assertEquals(new BigDecimal("100.00"), cuenta.getCurrentBalance());
    }

    @Test
    @DisplayName("The movement date defaults to now when not provided")
    void defaultsDateToNow() {
        Movimiento movimiento = cuenta("100").register(new BigDecimal("10"), null);

        assertTrue(movimiento.getFecha() != null);
    }

    @Test
    @DisplayName("A new account starts active and its balance equals the saldo inicial")
    void createsActiveAccount() {
        Cuenta cuenta = cuenta("2000");

        assertTrue(cuenta.isEstado());
        assertEquals(cuenta.getSaldoInicial(), cuenta.getCurrentBalance());
    }

    @Test
    @DisplayName("The saldo inicial cannot be negative")
    void rejectsNegativeInitialBalance() {
        assertThrows(DomainValidationException.class, () -> cuenta("-1"));
    }

    @Test
    @DisplayName("numeroCuenta is required")
    void rejectsBlankNumeroCuenta() {
        assertThrows(DomainValidationException.class,
                () -> Cuenta.create(" ", AccountType.AHORROS, BigDecimal.TEN, "jlema"));
    }
}
