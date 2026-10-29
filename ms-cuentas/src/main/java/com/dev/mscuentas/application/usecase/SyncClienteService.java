package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.SyncClienteCommand;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.port.in.SyncClienteUseCase;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Idempotent: a redelivered event is skipped by its eventId, and an out-of-order one is
 * discarded by the snapshot itself. Marking the event and updating the snapshot share one transaction.
 */
@Transactional
public class SyncClienteService implements SyncClienteUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncClienteService.class);

    private final ClienteSnapshotRepository snapshots;
    private final ProcessedEventRepository processedEvents;

    public SyncClienteService(ClienteSnapshotRepository snapshots, ProcessedEventRepository processedEvents) {
        this.snapshots = snapshots;
        this.processedEvents = processedEvents;
    }

    @Override
    public void sync(SyncClienteCommand command) {
        if (!processedEvents.markProcessed(command.eventId())) {
            log.info("Cliente event ignored, already processed: eventId={} clienteId={}",
                    command.eventId(), command.clienteId());
            return;
        }

        Optional<ClienteSnapshot> existing = snapshots.findByClienteId(command.clienteId());
        if (existing.isPresent()) {
            ClienteSnapshot snapshot = existing.get();
            boolean applied = command.deleted()
                    ? snapshot.markDeleted(command.occurredAt())
                    : snapshot.update(command.nombre(), command.estado(), command.occurredAt());
            if (applied) {
                snapshots.save(snapshot);
                log.info("Cliente copy {}: clienteId={} estado={}",
                        command.deleted() ? "deactivated (cliente deleted)" : "updated",
                        snapshot.getClienteId(), snapshot.isEstado());
            } else {
                log.info("Cliente event ignored, older than the stored copy: eventId={} clienteId={}",
                        command.eventId(), command.clienteId());
            }
        } else if (!command.deleted()) {
            snapshots.save(ClienteSnapshot.create(command.clienteId(), command.nombre(),
                    command.estado(), command.occurredAt()));
            log.info("Cliente copy created: clienteId={} estado={}", command.clienteId(), command.estado());
        } else {
            log.info("Cliente deletion ignored, unknown cliente: clienteId={}", command.clienteId());
        }
    }
}
