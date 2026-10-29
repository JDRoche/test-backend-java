package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.AccountStatementQuery;
import com.dev.mscuentas.application.dto.AccountStatementRow;
import com.dev.mscuentas.domain.exception.DomainValidationException;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.ClienteSnapshot;
import com.dev.mscuentas.domain.port.in.GetAccountStatementUseCase;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.StatementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class AccountStatementService implements GetAccountStatementUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountStatementService.class);

    private final ClienteSnapshotRepository snapshots;
    private final StatementRepository statements;

    public AccountStatementService(ClienteSnapshotRepository snapshots, StatementRepository statements) {
        this.snapshots = snapshots;
        this.statements = statements;
    }

    @Override
    public List<AccountStatementRow> getStatement(AccountStatementQuery query) {
        if (query.clienteId() == null || query.clienteId().isBlank()) {
            throw new DomainValidationException("El cliente es obligatorio");
        }
        if (query.from() == null || query.to() == null) {
            throw new DomainValidationException("El rango de fechas es obligatorio");
        }
        if (query.from().isAfter(query.to())) {
            throw new DomainValidationException("La fecha inicial no puede ser posterior a la fecha final");
        }

        ClienteSnapshot cliente = snapshots.findByClienteId(query.clienteId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + query.clienteId()));

        // the end date is inclusive: the whole day is covered by going up to the start of the next one
        List<AccountStatementRow> rows = statements.findLines(cliente.getClienteId(), query.from().atStartOfDay(),
                        query.to().plusDays(1).atStartOfDay())
                .stream()
                .map(line -> AccountStatementRow.of(cliente.getNombre(), line))
                .toList();
        log.info("Account statement generated: clienteId={} from={} to={} rows={}",
                cliente.getClienteId(), query.from(), query.to(), rows.size());
        return rows;
    }
}
