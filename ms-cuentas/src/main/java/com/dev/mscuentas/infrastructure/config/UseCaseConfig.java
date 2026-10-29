package com.dev.mscuentas.infrastructure.config;

import com.dev.mscuentas.application.usecase.AccountStatementService;
import com.dev.mscuentas.application.usecase.CuentaService;
import com.dev.mscuentas.application.usecase.MovimientoService;
import com.dev.mscuentas.application.usecase.SyncClienteService;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.CuentaRepository;
import com.dev.mscuentas.domain.port.out.MovimientoRepository;
import com.dev.mscuentas.domain.port.out.ProcessedEventRepository;
import com.dev.mscuentas.domain.port.out.StatementRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the use cases so that the application layer carries no stereotype annotations. */
@Configuration
public class UseCaseConfig {

    @Bean
    public SyncClienteService syncClienteService(ClienteSnapshotRepository snapshots,
                                                 ProcessedEventRepository processedEvents) {
        return new SyncClienteService(snapshots, processedEvents);
    }

    @Bean
    public CuentaService cuentaService(CuentaRepository cuentas, ClienteSnapshotRepository snapshots) {
        return new CuentaService(cuentas, snapshots);
    }

    @Bean
    public AccountStatementService accountStatementService(ClienteSnapshotRepository snapshots,
                                                            StatementRepository statements) {
        return new AccountStatementService(snapshots, statements);
    }

    @Bean
    public MovimientoService movimientoService(CuentaRepository cuentas, MovimientoRepository movimientos,
                                               ClienteSnapshotRepository snapshots) {
        return new MovimientoService(cuentas, movimientos, snapshots);
    }
}
