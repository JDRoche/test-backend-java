package com.dev.mscuentas.presentation.controller;

import com.dev.mscuentas.application.dto.MovimientoResult;
import com.dev.mscuentas.domain.port.in.QueryMovimientoUseCase;
import com.dev.mscuentas.domain.port.in.RegisterMovimientoUseCase;
import com.dev.mscuentas.domain.port.in.UpdateMovimientoUseCase;
import com.dev.mscuentas.presentation.request.RegisterMovimientoRequest;
import com.dev.mscuentas.presentation.request.UpdateMovimientoRequest;
import com.dev.mscuentas.presentation.response.MovimientoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final RegisterMovimientoUseCase registerMovimiento;
    private final UpdateMovimientoUseCase updateMovimiento;
    private final QueryMovimientoUseCase queryMovimiento;

    public MovimientoController(RegisterMovimientoUseCase registerMovimiento,
                                UpdateMovimientoUseCase updateMovimiento, QueryMovimientoUseCase queryMovimiento) {
        this.registerMovimiento = registerMovimiento;
        this.updateMovimiento = updateMovimiento;
        this.queryMovimiento = queryMovimiento;
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> register(@Valid @RequestBody RegisterMovimientoRequest request) {
        MovimientoResult result = registerMovimiento.register(request.toCommand());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(result.id()).toUri();
        return ResponseEntity.created(location).body(MovimientoResponse.from(result));
    }

    @GetMapping
    public List<MovimientoResponse> list(@RequestParam(required = false) String numeroCuenta) {
        return queryMovimiento.list(numeroCuenta).stream().map(MovimientoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MovimientoResponse getById(@PathVariable Long id) {
        return MovimientoResponse.from(queryMovimiento.getById(id));
    }

    /** Corrects the fecha of a movement. The valor and saldo are immutable. */
    @PatchMapping("/{id}")
    public MovimientoResponse update(@PathVariable Long id, @Valid @RequestBody UpdateMovimientoRequest request) {
        return MovimientoResponse.from(updateMovimiento.update(request.toCommand(id)));
    }
}
