package com.dev.mscuentas.infrastructure.persistence;

import com.dev.mscuentas.domain.model.StatementLine;
import com.dev.mscuentas.domain.port.out.StatementRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StatementPersistenceAdapter implements StatementRepository {

    private final MovimientoJpaRepository jpaRepository;

    public StatementPersistenceAdapter(MovimientoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<StatementLine> findLines(String clienteId, LocalDateTime from, LocalDateTime toExclusive) {
        return jpaRepository.findStatement(clienteId, from, toExclusive).stream()
                .map(m -> new StatementLine(m.getFecha(), m.getCuenta().getNumeroCuenta(),
                        m.getCuenta().getTipoCuenta(), m.getCuenta().getSaldoInicial(),
                        m.getCuenta().isEstado(), m.getValor(), m.getSaldo()))
                .toList();
    }
}
