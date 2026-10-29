@ignore
Feature: Creates a cuenta

  Scenario: create
    Given url baseUrl
    And path 'api', 'cuentas'
    And request { numeroCuenta: '#(__arg.numeroCuenta)', tipoCuenta: '#(__arg.tipoCuenta)', saldoInicial: '#(__arg.saldoInicial)', clienteId: '#(__arg.clienteId)' }
    When method post
    Then status 201
