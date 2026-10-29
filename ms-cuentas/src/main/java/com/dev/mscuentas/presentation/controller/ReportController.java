package com.dev.mscuentas.presentation.controller;

import com.dev.mscuentas.application.dto.AccountStatementQuery;
import com.dev.mscuentas.domain.port.in.GetAccountStatementUseCase;
import com.dev.mscuentas.presentation.request.DateRange;
import com.dev.mscuentas.presentation.response.AccountStatementResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    private final GetAccountStatementUseCase getAccountStatement;

    public ReportController(GetAccountStatementUseCase getAccountStatement) {
        this.getAccountStatement = getAccountStatement;
    }

    /**
     * "Estado de cuenta": the movements of every cuenta of a cliente within a date range.
     * Example: {@code /api/reportes?fecha=2022-02-08,2022-02-10&cliente=mmontalvo}
     *
     * @param fecha   range {@code yyyy-MM-dd,yyyy-MM-dd} (inclusive) or a single day {@code yyyy-MM-dd}
     * @param cliente the clienteId
     */
    @GetMapping
    public List<AccountStatementResponse> statement(@RequestParam("fecha") String fecha,
                                                    @RequestParam("cliente") String cliente) {
        DateRange range = DateRange.parse(fecha);
        return getAccountStatement.getStatement(new AccountStatementQuery(cliente, range.from(), range.to()))
                .stream()
                .map(AccountStatementResponse::from)
                .toList();
    }
}
