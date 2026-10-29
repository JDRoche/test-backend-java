Feature: Movimientos (create, read, update), balance update and "Saldo no disponible"

  Background:
    * url baseUrl
    * def clienteId = newClienteId()
    * call read('classpath:karate/support/publish-cliente.feature') { clienteId: '#(clienteId)', nombre: 'Jose Lema', estado: true }
    * call read('classpath:karate/support/wait-cliente.feature') { clienteId: '#(clienteId)' }

  Scenario Outline: Statement case 8.4 - <descripcion>
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: '<tipo>', saldoInicial: <saldoInicial>, clienteId: '#(clienteId)' }
    Given path 'api', 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: <valor> }
    When method post
    Then status 201
    And match response contains { numeroCuenta: '#(numero)', tipoMovimiento: '<tipoMovimiento>', valor: <valor>, saldo: <saldoEsperado> }

    Examples:
      | descripcion         | tipo      | saldoInicial | valor | tipoMovimiento | saldoEsperado |
      | retiro de 575       | AHORROS   | 2000         | -575  | RETIRO         | 1425          |
      | deposito de 600     | CORRIENTE | 100          | 600   | DEPOSITO       | 700           |
      | deposito de 150     | AHORROS   | 0            | 150   | DEPOSITO       | 150           |
      | retiro de 540       | AHORROS   | 540          | -540  | RETIRO         | 0             |

  Scenario: The balance of the cuenta follows the movements
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 1000, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: -200 }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 500 }
    Given path 'api', 'cuentas', numero
    When method get
    Then status 200
    And match response.currentBalance == 1300

  Scenario: A withdrawal without balance answers exactly "Saldo no disponible" (F3)
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 0, clienteId: '#(clienteId)' }
    Given path 'api', 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: -1 }
    When method post
    Then status 400
    And match response == { timestamp: '#string', status: 400, error: 'Bad Request', message: 'Saldo no disponible', path: '/api/movimientos' }

  Scenario: A rejected movement leaves the balance and the history untouched
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 100, clienteId: '#(clienteId)' }
    Given path 'api', 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: -100.01 }
    When method post
    Then status 400
    Given path 'api', 'cuentas', numero
    When method get
    Then status 200
    And match response.currentBalance == 100
    Given path 'api', 'movimientos'
    And param numeroCuenta = numero
    When method get
    Then status 200
    And match response == []

  Scenario: A movement of zero is rejected with 400
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'AHORROS', saldoInicial: 100, clienteId: '#(clienteId)' }
    Given path 'api', 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 0 }
    When method post
    Then status 400

  Scenario: A movement on an unknown cuenta answers 404
    Given path 'api', 'movimientos'
    And request { numeroCuenta: '000000', valor: 10 }
    When method post
    Then status 404
    And match response.message contains 'Cuenta no encontrada'

  Scenario: A movement can be registered with a past date and is listed most recent first
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 600, fecha: '2022-02-10T10:00:00' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: -50, fecha: '2022-02-09T10:00:00' }
    Given path 'api', 'movimientos'
    And param numeroCuenta = numero
    When method get
    Then status 200
    And match response == '#[2]'
    And match response[0].fecha == '2022-02-10T10:00:00'
    And match response[1].fecha == '2022-02-09T10:00:00'

  Scenario: Get a movimiento by id and correct its fecha
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, clienteId: '#(clienteId)' }
    * def created = call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 600, fecha: '2022-02-10T10:00:00' }
    * def id = created.response.id
    Given path 'api', 'movimientos', id
    When method get
    Then status 200
    And match response.saldo == 700
    Given path 'api', 'movimientos', id
    And request { fecha: '2022-02-10T12:30:00' }
    When method patch
    Then status 200
    And match response.fecha == '2022-02-10T12:30:00'
    And match response.valor == 600
    And match response.saldo == 700

  Scenario: Getting an unknown movimiento answers 404
    Given path 'api', 'movimientos', 999999999
    When method get
    Then status 404
