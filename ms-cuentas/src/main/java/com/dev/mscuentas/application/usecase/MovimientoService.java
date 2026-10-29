package com.dev.mscuentas.application.usecase;

import com.dev.mscuentas.application.dto.MovimientoResult;
import com.dev.mscuentas.application.dto.RegisterMovimientoCommand;
import com.dev.mscuentas.application.dto.UpdateMovimientoCommand;
import com.dev.mscuentas.domain.exception.ResourceNotFoundException;
import com.dev.mscuentas.domain.model.Cuenta;
import com.dev.mscuentas.domain.model.Movimiento;
import com.dev.mscuentas.domain.port.in.QueryMovimientoUseCase;
import com.dev.mscuentas.domain.port.in.RegisterMovimientoUseCase;
import com.dev.mscuentas.domain.port.in.UpdateMovimientoUseCase;
import com.dev.mscuentas.domain.port.out.ClienteSnapshotRepository;
import com.dev.mscuentas.domain.port.out.CuentaRepository;
import com.dev.mscuentas.domain.port.out.MovimientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class MovimientoService implements RegisterMovimientoUseCase, UpdateMovimientoUseCase, QueryMovimientoUseCase {

    private static final Logger log = LoggerFactory.getLogger(MovimientoService.class);

    private final CuentaRepository cuentas;
    private final MovimientoRepository movimientos;
    private final ActiveClienteChecker activeCliente;

    public MovimientoService(CuentaRepository cuentas, MovimientoRepository movimientos,
                             ClienteSnapshotRepository snapshots) {
        this.cuentas = cuentas;
        this.movimientos = movimientos;
        this.activeCliente = new ActiveClienteChecker(snapshots);
    }

    /**
     * Registers the movement and updates the balance in one transaction. The cuenta row is locked first,
     * so two concurrent movements on the same cuenta are applied one after the other and the balance
     * check always sees the latest value.
     */
    @Override
    public MovimientoResult register(RegisterMovimientoCommand command) {
        Cuenta cuenta = cuentas.findByNumeroCuentaForUpdate(command.numeroCuenta())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + command.numeroCuenta()));
        activeCliente.check(cuenta.getClienteId());

        Movimiento movimiento = cuenta.register(command.valor(), command.fecha());

        cuentas.save(cuenta);
        Movimiento saved = movimientos.save(movimiento);
        log.info("Movimiento registered: id={} numeroCuenta={} tipo={} valor={} saldo={}",
                saved.getId(), saved.getNumeroCuenta(), saved.getTipoMovimiento(), saved.getValor(), saved.getSaldo());
        return MovimientoResult.from(saved);
    }

    /** Only the fecha can be corrected: a wrong valor is fixed with a compensating movement. */
    @Override
    public MovimientoResult update(UpdateMovimientoCommand command) {
        Movimiento movimiento = findOrThrow(command.id());
        movimiento.reschedule(command.fecha());
        Movimiento saved = movimientos.save(movimiento);
        log.info("Movimiento fecha corrected: id={} fecha={}", saved.getId(), saved.getFecha());
        return MovimientoResult.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResult getById(Long id) {
        return MovimientoResult.from(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResult> list(String numeroCuenta) {
        List<Movimiento> found = numeroCuenta == null || numeroCuenta.isBlank()
                ? movimientos.findAll()
                : movimientos.findByNumeroCuenta(numeroCuenta);
        return found.stream().map(MovimientoResult::from).toList();
    }

    private Movimiento findOrThrow(Long id) {
        return movimientos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado: " + id));
    }
}
