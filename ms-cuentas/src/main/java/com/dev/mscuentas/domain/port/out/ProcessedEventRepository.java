package com.dev.mscuentas.domain.port.out;

import java.util.UUID;

public interface ProcessedEventRepository {

    /** Registers the event as processed. @return true if it was new, false if it had been processed before */
    boolean markProcessed(UUID eventId);
}
