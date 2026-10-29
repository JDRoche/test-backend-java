package com.dev.mscuentas.domain.port.in;

import com.dev.mscuentas.application.dto.AccountStatementQuery;
import com.dev.mscuentas.application.dto.AccountStatementRow;

import java.util.List;

public interface GetAccountStatementUseCase {

    List<AccountStatementRow> getStatement(AccountStatementQuery query);
}
