@ignore
Feature: Registers a movimiento

  Scenario: register
    * def body = { numeroCuenta: '#(__arg.numeroCuenta)', valor: '#(__arg.valor)' }
    * if (__arg.fecha) body.fecha = __arg.fecha
    Given url baseUrl
    And path 'api', 'movimientos'
    And request body
    When method post
    Then status 201
