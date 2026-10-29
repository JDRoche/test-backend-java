@ignore
Feature: Waits until MS Cuentas has received a cliente (the event is processed asynchronously)

  Scenario: wait
    * configure retry = { count: 40, interval: 500 }
    Given url baseUrl
    And path 'api', 'reportes'
    And param fecha = '2000-01-01'
    And param cliente = __arg.clienteId
    And retry until responseStatus == 200
    When method get
    Then status 200
