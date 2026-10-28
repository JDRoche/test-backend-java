package com.dev.msclientes.domain.port.out;

public interface PasswordHasher {

    String hash(String rawPassword);
}
