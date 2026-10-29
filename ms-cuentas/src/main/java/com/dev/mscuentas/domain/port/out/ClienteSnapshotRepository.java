package com.dev.mscuentas.domain.port.out;

import com.dev.mscuentas.domain.model.ClienteSnapshot;

import java.util.Optional;

public interface ClienteSnapshotRepository {

    ClienteSnapshot save(ClienteSnapshot snapshot);

    Optional<ClienteSnapshot> findByClienteId(String clienteId);
}
