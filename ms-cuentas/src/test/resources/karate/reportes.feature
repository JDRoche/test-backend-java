Feature: Reporte "Estado de cuenta" (F4)

  Background:
    * url baseUrl
    * def clienteId = newClienteId()
    * call read('classpath:karate/support/publish-cliente.feature') { clienteId: '#(clienteId)', nombre: 'Marianela Montalvo', estado: true }
    * call read('classpath:karate/support/wait-cliente.feature') { clienteId: '#(clienteId)' }

  Scenario: The statement of the guide (8.5) is returned with the exact JSON of 8.6
    * def corriente = newNumero()
    * def ahorros = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(corriente)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(ahorros)', tipoCuenta: 'AHORROS', saldoInicial: 540, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(corriente)', valor: 600, fecha: '2022-02-10T10:00:00' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(ahorros)', valor: -540, fecha: '2022-02-08T10:00:00' }
    Given path 'api', 'reportes'
    And param fecha = '2022-02-08,2022-02-10'
    And param cliente = clienteId
    When method get
    Then status 200
    And match response ==
      """
      [
        { 'Fecha': '10/2/2022', 'Cliente': 'Marianela Montalvo', 'Numero Cuenta': '#(corriente)', 'Tipo': 'Corriente', 'Saldo Inicial': 100, 'Estado': true, 'Movimiento': 600, 'Saldo Disponible': 700 },
        { 'Fecha': '8/2/2022', 'Cliente': 'Marianela Montalvo', 'Numero Cuenta': '#(ahorros)', 'Tipo': 'Ahorros', 'Saldo Inicial': 540, 'Estado': true, 'Movimiento': -540, 'Saldo Disponible': 0 }
      ]
      """

  Scenario: The date range filters the movements and its end date is inclusive
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 1000, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 10, fecha: '2022-02-07T23:59:59' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 20, fecha: '2022-02-08T00:00:00' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 30, fecha: '2022-02-10T23:59:59' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 40, fecha: '2022-02-11T00:00:00' }
    Given path 'api', 'reportes'
    And param fecha = '2022-02-08,2022-02-10'
    And param cliente = clienteId
    When method get
    Then status 200
    And match response == '#[2]'
    And match response[*].Movimiento == [30, 20]

  Scenario: A single day is a range of one day
    * def numero = newNumero()
    * call read('classpath:karate/support/create-cuenta.feature') { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 100, clienteId: '#(clienteId)' }
    * call read('classpath:karate/support/register-movimiento.feature') { numeroCuenta: '#(numero)', valor: 5, fecha: '2022-03-15T08:00:00' }
    Given path 'api', 'reportes'
    And param fecha = '2022-03-15'
    And param cliente = clienteId
    When method get
    Then status 200
    And match response == '#[1]'

  Scenario: A cliente without movements in the range gets an empty list
    Given path 'api', 'reportes'
    And param fecha = '2030-01-01,2030-01-02'
    And param cliente = clienteId
    When method get
    Then status 200
    And match response == []

  Scenario Outline: Invalid report requests are rejected with 400 - <caso>
    Given path 'api', 'reportes'
    And params <params>
    When method get
    Then status 400
    And match response.message contains '<mensaje>'

    Examples:
      | caso                | params                                                    | mensaje   |
      | sin fecha           | { cliente: 'x' }                                          | fecha     |
      | sin cliente         | { fecha: '2022-02-08,2022-02-10' }                        | cliente   |
      | formato invalido    | { fecha: '10-02-2022', cliente: 'x' }                     | formato   |
      | rango invertido     | { fecha: '2022-02-10,2022-02-08', cliente: 'x' }          | posterior |

  Scenario: A report for an unknown cliente answers 404
    Given path 'api', 'reportes'
    And param fecha = '2022-02-08,2022-02-10'
    And param cliente = 'no-existe'
    When method get
    Then status 404
    And match response.message contains 'Cliente no encontrado'
