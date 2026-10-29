package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.SyncClienteCommand;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.ProcessedEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncClienteServiceTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private final ClienteSnapshotRepository snapshots = mock(ClienteSnapshotRepository.class);
    private final ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
    private final SyncClienteService service = new SyncClienteService(snapshots, processedEvents);

    private SyncClienteCommand created(Instant at) {
        return SyncClienteCommand.upsert(UUID.randomUUID(), at, "jlema", "Jose Lema", true);
    }

    private ClienteSnapshot savedSnapshot() {
        ArgumentCaptor<ClienteSnapshot> captor = ArgumentCaptor.forClass(ClienteSnapshot.class);
        verify(snapshots).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("A created event for an unknown cliente stores a new snapshot")
    void createsSnapshot() {
        when(processedEvents.markProcessed(any())).thenReturn(true);
        when(snapshots.findByClienteId("jlema")).thenReturn(Optional.empty());

        service.sync(created(T0));

        ClienteSnapshot saved = savedSnapshot();
        assertEquals("Jose Lema", saved.getNombre());
        assertTrue(saved.isEstado());
    }

    @Test
    @DisplayName("A redelivered event (already processed) is ignored")
    void ignoresDuplicateEvent() {
        when(processedEvents.markProcessed(any())).thenReturn(false);

        service.sync(created(T0));

        verify(snapshots, never()).save(any());
    }

    @Test
    @DisplayName("An update newer than the snapshot is applied")
    void appliesNewerUpdate() {
        when(processedEvents.markProcessed(any())).thenReturn(true);
        when(snapshots.findByClienteId("jlema"))
                .thenReturn(Optional.of(ClienteSnapshot.create("jlema", "Jose Lema", true, T0)));

        service.sync(SyncClienteCommand.upsert(UUID.randomUUID(), T0.plusSeconds(5), "jlema", "Jose L.", false));

        ClienteSnapshot saved = savedSnapshot();
        assertEquals("Jose L.", saved.getNombre());
        assertFalse(saved.isEstado());
    }

    @Test
    @DisplayName("An update older than the snapshot (out of order) is discarded")
    void discardsStaleUpdate() {
        when(processedEvents.markProcessed(any())).thenReturn(true);
        when(snapshots.findByClienteId("jlema"))
                .thenReturn(Optional.of(ClienteSnapshot.create("jlema", "Jose Lema", true, T0)));

        service.sync(SyncClienteCommand.upsert(UUID.randomUUID(), T0.minusSeconds(5), "jlema", "Viejo", false));

        verify(snapshots, never()).save(any());
    }

    @Test
    @DisplayName("A deleted event deactivates the snapshot instead of removing it")
    void deletionDeactivatesSnapshot() {
        when(processedEvents.markProcessed(any())).thenReturn(true);
        when(snapshots.findByClienteId("jlema"))
                .thenReturn(Optional.of(ClienteSnapshot.create("jlema", "Jose Lema", true, T0)));

        service.sync(SyncClienteCommand.deletion(UUID.randomUUID(), T0.plusSeconds(1), "jlema"));

        ClienteSnapshot saved = savedSnapshot();
        assertFalse(saved.isEstado());
        assertEquals("Jose Lema", saved.getNombre());
    }

    @Test
    @DisplayName("A late created event cannot bring back a deleted cliente")
    void lateCreatedDoesNotResurrect() {
        ClienteSnapshot deleted = ClienteSnapshot.create("jlema", "Jose Lema", true, T0);
        deleted.markDeleted(T0.plusSeconds(10));
        when(processedEvents.markProcessed(any())).thenReturn(true);
        when(snapshots.findByClienteId("jlema")).thenReturn(Optional.of(deleted));

        service.sync(created(T0.plusSeconds(1)));

        verify(snapshots, never()).save(any());
        assertFalse(deleted.isEstado());
    }

    @Test
    @DisplayName("A deleted event for an unknown cliente stores nothing")
    void deletionOfUnknownClienteIsIgnored() {
        when(processedEvents.markProcessed(any())).thenReturn(true);
        when(snapshots.findByClienteId("jlema")).thenReturn(Optional.empty());

        service.sync(SyncClienteCommand.deletion(UUID.randomUUID(), T0, "jlema"));

        verify(snapshots, never()).save(any());
    }
}
