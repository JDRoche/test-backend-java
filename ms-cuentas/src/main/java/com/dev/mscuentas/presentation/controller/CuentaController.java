package com.dev.mscuentas.presentation.controller;

import com.dev.mscuentas.application.dto.CuentaResult;
import com.dev.mscuentas.domain.port.in.CreateCuentaUseCase;
import com.dev.mscuentas.domain.port.in.QueryCuentaUseCase;
import com.dev.mscuentas.domain.port.in.UpdateCuentaUseCase;
import com.dev.mscuentas.presentation.request.CreateCuentaRequest;
import com.dev.mscuentas.presentation.request.UpdateCuentaRequest;
import com.dev.mscuentas.presentation.response.CuentaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CreateCuentaUseCase createCuenta;
    private final UpdateCuentaUseCase updateCuenta;
    private final QueryCuentaUseCase queryCuenta;

    public CuentaController(CreateCuentaUseCase createCuenta, UpdateCuentaUseCase updateCuenta,
                            QueryCuentaUseCase queryCuenta) {
        this.createCuenta = createCuenta;
        this.updateCuenta = updateCuenta;
        this.queryCuenta = queryCuenta;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> create(@Valid @RequestBody CreateCuentaRequest request) {
        CuentaResult result = createCuenta.create(request.toCommand());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numeroCuenta}").buildAndExpand(result.numeroCuenta()).toUri();
        return ResponseEntity.created(location).body(CuentaResponse.from(result));
    }

    @GetMapping
    public List<CuentaResponse> list(@RequestParam(required = false) String clienteId) {
        return queryCuenta.list(clienteId).stream().map(CuentaResponse::from).toList();
    }

    @GetMapping("/{numeroCuenta}")
    public CuentaResponse getByNumeroCuenta(@PathVariable String numeroCuenta) {
        return CuentaResponse.from(queryCuenta.getByNumeroCuenta(numeroCuenta));
    }

    @PutMapping("/{numeroCuenta}")
    public CuentaResponse update(@PathVariable String numeroCuenta, @Valid @RequestBody UpdateCuentaRequest request) {
        return CuentaResponse.from(updateCuenta.update(request.toCommand(numeroCuenta)));
    }
}
