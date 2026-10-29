@ignore
Feature: Creates a cliente

  Scenario: create
    Given url baseUrl
    And path 'api', 'clientes'
    And request { clienteId: '#(__arg.clienteId)', nombre: 'Cliente de prueba', identificacion: '#(__arg.identificacion)', contrasena: '1234' }
    When method post
    Then status 201
