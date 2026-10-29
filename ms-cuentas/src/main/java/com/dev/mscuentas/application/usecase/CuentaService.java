package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.CreateCuentaCommand;
import com.dev.mscuentas.application.dto.CuentaResult;
import com.dev.mscuentas.application.dto.UpdateCuentaCommand;
import com.dev.mscuentas.domain.exception.DuplicateCuentaException;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.Cuenta;
import com.dev.mscuentas.domain.port.in.CreateCuentaUseCase;
import com.dev.mscuentas.domain.port.in.QueryCuentaUseCase;
import com.dev.mscuentas.domain.port.in.UpdateCuentaUseCase;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.CuentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class CuentaService implements CreateCuentaUseCase, UpdateCuentaUseCase, QueryCuentaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CuentaService.class);

    private final CuentaRepository cuentas;
    private final ActiveClienteChecker activeCliente;

    public CuentaService(CuentaRepository cuentas, ClienteSnapshotRepository snapshots) {
        this.cuentas = cuentas;
        this.activeCliente = new ActiveClienteChecker(snapshots);
    }

    @Override
    public CuentaResult create(CreateCuentaCommand command) {
        Cuenta cuenta = Cuenta.create(command.numeroCuenta(), command.tipoCuenta(),
                command.saldoInicial(), command.clienteId());
        if (cuentas.existsByNumeroCuenta(cuenta.getNumeroCuenta())) {
            throw new DuplicateCuentaException("Ya existe la cuenta " + cuenta.getNumeroCuenta());
        }
        activeCliente.check(cuenta.getClienteId());
        Cuenta saved = cuentas.save(cuenta);
        log.info("Cuenta created: numeroCuenta={} tipo={} saldoInicial={} clienteId={}",
                saved.getNumeroCuenta(), saved.getTipoCuenta(), saved.getSaldoInicial(), saved.getClienteId());
        return CuentaResult.from(saved);
    }

    @Override
    public CuentaResult update(UpdateCuentaCommand command) {
        Cuenta cuenta = findOrThrow(command.numeroCuenta());
        cuenta.update(command.tipoCuenta(), command.estado());
        Cuenta saved = cuentas.save(cuenta);
        log.info("Cuenta updated: numeroCuenta={} tipo={} estado={}",
                saved.getNumeroCuenta(), saved.getTipoCuenta(), saved.isEstado());
        return CuentaResult.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResult getByNumeroCuenta(String numeroCuenta) {
        return CuentaResult.from(findOrThrow(numeroCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResult> list(String clienteId) {
        List<Cuenta> found = clienteId == null || clienteId.isBlank()
                ? cuentas.findAll()
                : cuentas.findByClienteId(clienteId);
        return found.stream().map(CuentaResult::from).toList();
    }

    private Cuenta findOrThrow(String numeroCuenta) {
        return cuentas.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
    }
}
