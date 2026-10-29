Feature: Cuentas (create, read, update)

  Background:
    * url baseUrl
    * def clienteId = newClienteId()
    * call read('classpath:karate/support/publish-cliente.feature') { clienteId: '#(clienteId)', nombre: 'Marianela Montalvo', estado: true }
    * call read('classpath:karate/support/wait-cliente.feature') { clienteId: '#(clienteId)' }

  Scenario: Create a cuenta for an active cliente (8.2)
    * def numero = newNumero()
    Given path 'api', 'cuentas'
    And request { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, clienteId: '#(clienteId)' }
    When method post
    Then status 201
    And match response == { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, currentBalance: 100, estado: true, clienteId: '#(clienteId)' }

  Scenario: A duplicated numeroCuenta is rejected with 409
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 10, clienteId: '#(clienteId)' }
    Given path 'api', 'cuentas'
    And request { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 10, clienteId: '#(clienteId)' }
    When method post
    Then status 409
    And match response.message contains 'Ya existe la cuenta'

  Scenario: A cuenta for an unknown cliente is rejected with 404
    Given path 'api', 'cuentas'
    And request { numeroCuenta: '#(newNumero())', tipoCuenta: 'AHORROS', saldoInicial: 10, clienteId: 'no-existe' }
    When method post
    Then status 404
    And match response.message contains 'Cliente no encontrado'

  Scenario: A negative saldo inicial is rejected with 400
    Given path 'api', 'cuentas'
    And request { numeroCuenta: '#(newNumero())', tipoCuenta: 'AHORROS', saldoInicial: -5, clienteId: '#(clienteId)' }
    When method post
    Then status 400

  Scenario: An invalid tipoCuenta is rejected with 400
    Given path 'api', 'cuentas'
    And request { numeroCuenta: '#(newNumero())', tipoCuenta: 'PLATINO', saldoInicial: 1, clienteId: '#(clienteId)' }
    When method post
    Then status 400

  Scenario: Get a cuenta by its numero
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 2000, clienteId: '#(clienteId)' }
    Given path 'api', 'cuentas', numero
    When method get
    Then status 200
    And match response.currentBalance == 2000
    And match response.estado == true

  Scenario: List the cuentas of a cliente
    * def first = newNumero()
    * def second = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(first)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(second)', tipoCuenta: 'AHORROS', saldoInicial: 540, clienteId: '#(clienteId)' }
    Given path 'api', 'cuentas'
    And param clienteId = clienteId
    When method get
    Then status 200
    And match response == '#[2]'
    And match each response contains { clienteId: '#(clienteId)' }

  Scenario: Update the tipo and the estado of a cuenta
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 1000, clienteId: '#(clienteId)' }
    Given path 'api', 'cuentas', numero
    And request { tipoCuenta: 'AHORROS', estado: false }
    When method put
    Then status 200
    And match response contains { tipoCuenta: 'AHORROS', estado: false, currentBalance: 1000 }

  Scenario: Getting an unknown cuenta answers 404
    Given path 'api', 'cuentas', '000000'
    When method get
    Then status 404
