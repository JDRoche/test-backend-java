package com.dev.mscuentas.domain.exception;

public class InsufficientBalanceException extends DomainException {

    public static final String MESSAGE = "Saldo no disponible";

    public InsufficientBalanceException() {
        super(MESSAGE);
    }
}
