package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ClienteSnapshotPersistenceAdapter implements ClienteSnapshotRepository {

    private final ClienteSnapshotJpaRepository jpaRepository;

    public ClienteSnapshotPersistenceAdapter(ClienteSnapshotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ClienteSnapshot save(ClienteSnapshot snapshot) {
        return toDomain(jpaRepository.save(toEntity(snapshot)));
    }

    @Override
    public Optional<ClienteSnapshot> findByClienteId(String clienteId) {
        return jpaRepository.findById(clienteId).map(this::toDomain);
    }

    private ClienteSnapshot toDomain(ClienteSnapshotJpaEntity entity) {
        return ClienteSnapshot.rehydrate(entity.getClienteId(), entity.getNombre(),
                entity.isEstado(), entity.getLastEventAt());
    }

    private ClienteSnapshotJpaEntity toEntity(ClienteSnapshot snapshot) {
        ClienteSnapshotJpaEntity entity = new ClienteSnapshotJpaEntity();
        entity.setClienteId(snapshot.getClienteId());
        entity.setNombre(snapshot.getNombre());
        entity.setEstado(snapshot.isEstado());
        entity.setLastEventAt(snapshot.getLastEventAt());
        return entity;
    }
}
