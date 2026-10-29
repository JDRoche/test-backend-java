package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.port.out.ProcessedEventRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

/**
 * The primary key on event_id guards against two consumers handling the same event at once:
 * the loser fails on flush and its message is retried, finding the event already processed.
 */
@Repository
public class ProcessedEventPersistenceAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    public ProcessedEventPersistenceAdapter(ProcessedEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean markProcessed(UUID eventId) {
        if (jpaRepository.existsById(eventId)) {
            return false;
        }
        ProcessedEventJpaEntity entity = new ProcessedEventJpaEntity();
        entity.setEventId(eventId);
        entity.setProcessedAt(Instant.now());
        jpaRepository.saveAndFlush(entity);
        return true;
    }
}
