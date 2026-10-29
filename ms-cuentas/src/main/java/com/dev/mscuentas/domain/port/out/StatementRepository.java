package com.dev.mscuentas.domain.port.out;

import com.dev.mscuentas.domain.model.StatementLine;

import java.time.LocalDateTime;
import java.util.List;

public interface StatementRepository {

    /**
     * Movements of every cuenta of the cliente with {@code from <= fecha < toExclusive},
     * most recent first, fetched in a single query.
     */
    List<StatementLine> findLines(String clienteId, LocalDateTime from, LocalDateTime toExclusive);
}
