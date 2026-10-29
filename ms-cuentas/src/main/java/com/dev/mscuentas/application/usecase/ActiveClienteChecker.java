package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;

/** Business rule shared by the use cases: a cuenta can only operate for a known and active cliente. */
class ActiveClienteChecker {

    private final ClienteSnapshotRepository snapshots;

    ActiveClienteChecker(ClienteSnapshotRepository snapshots) {
        this.snapshots = snapshots;
    }

    void check(String clienteId) {
        ClienteSnapshot cliente = snapshots.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));
        if (!cliente.isEstado()) {
            throw new DomainValidationException("El cliente " + clienteId + " no está activo");
        }
    }
}
